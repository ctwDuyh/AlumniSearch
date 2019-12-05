package whu.alumnispider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;
import whu.alumnispider.DAO.AlumniDAO;
import whu.alumnispider.parser.KeywordParser;
import whu.alumnispider.utilities.Alumni;
import whu.alumnispider.utilities.College;
import whu.alumnispider.utilities.GovLeaderPerson;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaiduSearchProcessor implements PageProcessor {
    //private static String personLinkRe = "https://baike.baidu.com/item/";
    //private static Pattern personLinkPattern = Pattern.compile(personLinkRe);
    private AlumniDAO alumniDAO = new AlumniDAO();
    private boolean firstSearch = true;
    private Site site = Site.me().setSleepTime(150).setRetryTimes(2)
            .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

    public String tableName = "alumni";
    private static List<String> searchNameList;
    private static final String[] SCHOOLNAME = {"武汉大学","武汉水利电力大学","武汉测绘科技大学","湖北医科大学",
            "湖北水利水电学院", "葛洲坝水电工程学院","武汉测绘学院","武汉测量制图学院","湖北医学院","湖北省医学院",
            "湖北省立医学院","武汉水利电力学院","武汉水利水电学院"};
    private static final String[] ILLEGALWORDS = {"违纪","违法"};
    private static final String[] PERSONILLEGALWORDS = {"涉嫌严重","因严重"};
    private static HashSet<String> personHashSet = new HashSet<>();


    @Override
    public Site getSite() {
        return site;
    }


    @Override
    public void process(Page page) {
        // ResultItem Selectable
        Selectable personPage;

        List<String> personPages = new ArrayList<String>();

        // Below code is the definition of Xpath sentence.
        String personLinkXpath = "//ul[@class='polysemantList-wrapper cmn-clearfix']/li/a/@href";
        String personLinkXpath2 = "//ul[@class='custom_dot  para-list list-paddingleft-1']/li/div/a/@href";
        String personNamePath = "https://baike\\.baidu\\.com/item/(.*)";
        //String processingUrl = page.getUrl().toString();
        //Matcher personLinkMatcher = personLinkPattern.matcher(processingUrl);
        String name = getMatching(page.getUrl().toString(),personNamePath);
        // 第一次搜索，才爬取其他目录的网址
        if (searchNameList.contains(name)){
            // 寻找其他目录
            personPage = page.getHtml().xpath(personLinkXpath);
            personPages = personPage.all();
            for(String tempPage : personPages){
                Request request = new Request(tempPage);
                page.addTargetRequest(request);
            }
            personPage = page.getHtml().xpath(personLinkXpath2);
            personPages = personPage.all();
            for(String tempPage : personPages){
                tempPage = tempPage + "#viewPageContent";
                Request request = new Request(tempPage);
                page.addTargetRequest(request);
            }
        }
        if (isPersonRelated2Whu(page)){
            getInformation(page);
        }


    }
    //
    private boolean isPersonRelated2Whu(Page page){

        String entryTextXpath = "//dd/allText()";
        String mainTextXpath = "//div[@class='para']/allText()";
        // 寻找当前目录是否与武大有关
        Selectable personWord;
        List<String> personWords = new ArrayList<String>();
        // 先检索词条信息
        personWord = page.getHtml().xpath(entryTextXpath);
        personWords = personWord.all();
        if (isWordRelated2Whu(personWords)){
            // test
            //System.out.println("人物词条匹配成功");
            String url = page.getUrl().toString();
            System.out.println("匹配成功的网址为:"+url);
            return true;
        }
        else{
            // 词条不匹配，再检索人物的主要信息
            personWord = page.getHtml().xpath(mainTextXpath);
            personWords = personWord.all();
            if (isWordRelated2Whu(personWords)){
                //System.out.println("人物主要内容匹配成功");
                String url = page.getUrl().toString();
                System.out.println("匹配成功的网址为:"+url);
                return true;
            }
        }

        return false;
    }

    private boolean isPersonRelated2Illegal(Page page){
        String mainTextXpath = "//div[@class='para']/allText()";
        Selectable personWord;
        List<String> personWords;
        personWord = page.getHtml().xpath(mainTextXpath);
        personWords = personWord.all();
        return isWordRelated2Illegal(personWords);
    }

    private boolean isWordRelated2Whu(List<String> personWords){
        for (String schoolName : SCHOOLNAME){
            for (String word : personWords)
                if (word.contains(schoolName))
                    return true;
        }
        return false;
    }

    private boolean isWordRelated2Illegal(List<String> personWords){
        for (String illegalWord : ILLEGALWORDS){
            for (String word : personWords)
                if (word.contains(illegalWord)){
                    for (String personIllegalWord : PERSONILLEGALWORDS){
                        if (word.contains(personIllegalWord))
                            return true;
                    }
                }
        }
        return false;
    }

    // get person job
    private void getInformation(Page page){
        String personJobInfoPath1 = "//dd[@class='lemmaWgt-lemmaTitle-title']/h2/text()";
        String personJobInfoPath2 = "//div[@class='lemma-summary']/div[@class='para']/allText()";
        String personJobInfoPath3 = "//div[@class='basic-info cmn-clearfix']//dd/text()";
        String personNamePath = "//dd[@class='lemmaWgt-lemmaTitle-title']/h1/text()";
        String[] personJobPath2s = {"现任(.*?)。","职业为(.*?)。","现为(.*?)。","现系(.*?)。","现任(.*?)；","职业为(.*?)；","现为(.*?)；","现系(.*?)；"};
        String personJobPath1 = "（(.*)）";
        String indexPath = "\\[(\\d*?)\\]";
        String blankPath = "(\\s|\\u00A0)*";
        Selectable personJobInfoPage;
        String personJob;
        Selectable personNamePage;
        String personName;
        List<String> personJobInfos = new ArrayList<>();
        boolean isIllegal = false;
        Alumni person = new Alumni();

        personJobInfoPage = page.getHtml().xpath(personJobInfoPath1);
        personJob = personJobInfoPage.toString();
        personNamePage = page.getHtml().xpath(personNamePath);
        personName = personNamePage.toString();
        // 判断从匹配模式一是否能够获取职业信息
        if (personJob!=null){
            personJob = getMatching(personJob,personJobPath1);
        }
        else {
            // 从匹配模式二获取职业信息
            personJobInfoPage = page.getHtml().xpath(personJobInfoPath2);
            personJobInfos = personJobInfoPage.all();
            personJob = getMatching(personJobInfos,personJobPath2s);
            if (personJob==null){
                personJobInfoPage = page.getHtml().xpath(personJobInfoPath3);
                personJobInfos = personJobInfoPage.all();
                personJob = getMatching(personJobInfos,personJobPath2s);
            }

            // 从匹配模式三获取职业信息(暂不需要)
        }
        if (isPersonRelated2Illegal(page)){
            isIllegal = true;
            System.out.println("注意："+personName+"涉嫌违法违纪。");
        }
        String url = page.getUrl().toString();
        if (personJob!=null){
            personJob = personJob.replaceAll(indexPath,"");
            personJob = personJob.replaceAll(blankPath,"");
            System.out.println("人物姓名："+personName+" , 人物信息："+personJob);
        }
        person.setName(personName);
        person.setJob(personJob);
        person.setIllegal(isIllegal);
        person.setWebsite(url);
        alumniDAO.add(person,"alumni");
    }
    // 用于职业匹配模式二
    /*
    private String getMatching(String soap,String[] rgexs){
        for (String rgex : rgexs){
            Pattern pattern = Pattern.compile(rgex);
            Matcher m = pattern.matcher(soap);
            if(m.find()){
                return m.group(1);
            }
        }
        return null;
    }
    */
    private String getMatching(List<String> soaps,String[] rgexs){
        for (String soap : soaps){
            for (String rgex : rgexs){
                Pattern pattern = Pattern.compile(rgex);
                Matcher m = pattern.matcher(soap);
                if(m.find()){
                    return m.group(1);
                }
            }
        }
        return null;
    }

    // 用于职业匹配模式一
    private String getMatching(String soap,String rgex){
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(soap);
        if(m.find()){
            return m.group(1);
        }
        return soap;
    }


    public static void main(String[] args) {
        /*
        searchNameList = Arrays.asList("万鄂湘","辜胜阻","陈小江","解振华","李金早", "李小林","李晓红","刘宁","鹿心社",
                "阮成发","孙志刚","孙志军","庹震");

        searchNameList = Arrays.asList("张军","安东","陈安丽","陈飞","邓中华","邓恢林","范恒山","范锐平","冯俊","甘霖",
                "宫鸣","郭生练","何泽中","韩卫江","胡孝汉","胡振鹏");

        searchNameList = Arrays.asList("黄宪起","黄海龙","黄俊华","蒋旭光","焦红","贾宇","柯良栋","李惠东","李建明",
                "李军","李维森","李晓鹏","梁伟年","刘雅鸣");

        searchNameList = Arrays.asList("刘晓鸣","柳芳","卢雍政","吕忠梅","罗东川","罗文","闵宜仁","随忠诚","彭佳学",
                "彭克玉","谭作钧","汤敏","唐军","陶凯元","田立文");

        searchNameList = Arrays.asList("田湘利","汪鸿雁","王玲","王少峰","王艳玲","王一新","魏海生","魏山忠","吴恳",
                "肖云刚","谢晓尧","熊选国","徐显明","薛晓峰","薛江武","杨云彦","尹中卿");

        searchNameList = Arrays.asList("岳中明","詹成付","张京泽","张鸣","张硕辅","张维宁","张野","郑功成","周汉民",
                "周维现","白耀华","鲍常勇","鲍遂献","别必雄","蔡玲","陈晋");

        searchNameList = Arrays.asList("池莉","丁仁立","丁尚清","丁小强","董石桂","付子堂","高玉葆","官景辉","郭全茂",
                "郭永航","黄进","黄泰岩","姜文波","蒋昌忠");

        searchNameList = Arrays.asList("金学峰","邝兵","李伏安","李宏伟","李建林","李鹏德","李清泉","李庆雄","李全战",
                "吕文艳","李伟华","李莹","李志刚");

        searchNameList = Arrays.asList("廖立强","刘传铁","刘汉俊","刘慕仁","鲁毅","马黎","欧阳玉靖","彭震中","乔余堂",
                "宋灵恩","苏杨","谭仁杰","万速成","王本朝","王本强","王春峰","王广正","王立新");

        searchNameList = Arrays.asList("魏凤君","吴国凯","吴祖云","武文忠","夏光","夏先鹏","鲜铁可","向东","肖勤福",
                "谢红星","熊召政","徐宏","徐金鹏","许尔文","晏蒲柳","杨宏山");

        searchNameList = Arrays.asList("杨志坚","姚庆海","易树柏","殷昭举","余龙华","岳晓勇","张辉峰","张劲","张志川",
                "张志颇","赵立成","郑俊康","郑雁雄","周潮洪");

        searchNameList = Arrays.asList("周创兵","周天鸿","李桥铭","吴社洲","朱列玉","魏青松","蔡华东","鲍英华","陈春林",
                "鲍绍坤","车延高","陈军","程蔚东","陈俊宏","仇小乐","曹玉书");
        */
        searchNameList = Arrays.asList("曹玉书");
        List<String> urls = new ArrayList<>();
        for (String name : searchNameList){
            urls.add("https://baike.baidu.com/item/"+name);
        }
        String[] urlArray = new String[urls.size()];
        urls.toArray(urlArray);
        Spider.create(new BaiduSearchProcessor())
                .addUrl(urlArray)
                .thread(3)
                .run();



    }
}
