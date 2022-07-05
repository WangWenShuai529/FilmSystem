package com.panda.web.controller.system;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.panda.common.constant.MovieRankingList;
import com.panda.common.response.ResponseResult;
import com.panda.common.utils.JedisPoolUtils;
import com.panda.system.domin.SysMovie;
import com.panda.system.domin.vo.SysMovieVo;
import com.panda.system.service.impl.SysMovieServiceImpl;
import com.panda.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


@RestController
public class SysMovieController extends BaseController {

    @Autowired
    private SysMovieServiceImpl sysMovieService;


    @GetMapping("/sysMovie/find")
    public ResponseResult findAllMovies(SysMovieVo sysMovieVo) {
        startPage();
        List<SysMovie> data = sysMovieService.findAllMovies(sysMovieVo);
        return getResult(data);
    }

    @GetMapping("/sysMovie/find/{id}")
    public ResponseResult findMovieById(@PathVariable Long id) {
        return getResult(sysMovieService.findMovieById(id));
    }

    @PostMapping("/sysMovie")
    public ResponseResult addMovie(@Validated @RequestBody SysMovie sysMovie) {
        return getResult(sysMovieService.addMovie(sysMovie));
    }

    @PutMapping("/sysMovie")
    public ResponseResult updateMovie(@Validated @RequestBody SysMovie sysMovie) {
        return getResult(sysMovieService.updateMovie(sysMovie));
    }

    @DeleteMapping("/sysMovie/{ids}")
    public ResponseResult deleteMovie(@PathVariable Long[] ids) {
        return getResult(sysMovieService.deleteMovie(ids));
    }

//    不用redis，可以使用反射
    @GetMapping("/sysMovie/find/rankingList/1/{listId}")
    public ResponseResult findRankingList(@PathVariable Integer listId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (listId <= 0 || listId > 4) {
            //暂时只支持4种榜单
            return ResponseResult.error("抱歉，暂时只支持3种榜单，id为[1,3]");
        }
//        根据反射
//        listNames[0] = "totalBoxOfficeList";
//        listNames[1] = "domesticBoxOfficeList";
//        listNames[2] = "foreignBoxOfficeList";

        Method getList = sysMovieService.getClass().getMethod(MovieRankingList.listNames[listId - 1]);
        startPage();
        List<SysMovie> data = (List<SysMovie>) getList.invoke(sysMovieService);
        return getResult(data);
    }

    @GetMapping("/sysMovie/find/rankingList/{listId}")
    public ResponseResult findRankingListtest(@PathVariable Integer listId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {


        //1.先从redis中查询数据
        //1.1获取redis客户端连接
        Jedis jedis = JedisPoolUtils.getJedis();
        String totalBoxOfficeListJson = jedis.get("totalBoxOfficeList");
        String domesticBoxOfficeListJson = jedis.get("domesticBoxOfficeList");
        String foreignBoxOfficeListJson = jedis.get("foreignBoxOfficeList");

        if(listId == 1){
//            "totalBoxOfficeList";
            //        2判断 redis的缓存数据 数据是否为null
//        totalBoxOfficeListJson
            if(totalBoxOfficeListJson == null || totalBoxOfficeListJson.length() == 0){
                //redis为空，从数据库查询，并将结果保存在数据库中
                System.out.println("redis为空，从数据库查询");
                //2.1调用service——>dao,查询数据
                List<SysMovie> data = sysMovieService.totalBoxOfficeList();
                //查询并序列化为json字符串
                String json = new Gson().toJson(data);
                System.out.println("json:"+json);
                List<SysMovie> ps = data;
                //2.2将list序列化为json
                ObjectMapper mapper = new ObjectMapper();
                try {
                    totalBoxOfficeListJson = mapper.writeValueAsString(ps);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                //2.3 将查询的结果，存入redis缓存
                jedis.set("totalBoxOfficeList",totalBoxOfficeListJson);
                //释放连接池资源关闭
                jedis.close();

                System.out.println("json:"+json);
                return getResult(data);

            }else{
                System.out.println("redis成功...");
                String obj=totalBoxOfficeListJson;
                System.out.println("totalBoxOfficeListJson："+totalBoxOfficeListJson);
                Gson gson = new Gson();
//            解析json数据
                List<SysMovie> data = JSON.parseArray(totalBoxOfficeListJson, SysMovie.class);
//            for(SysMovie u :data){
//                System.out.println(u.toString());
//            }
                return getResult(data);
            }

        }else if(listId == 2){
//            "domesticBoxOfficeList";
            //        2判断 redis的缓存数据 数据是否为null
            if(domesticBoxOfficeListJson == null || domesticBoxOfficeListJson.length() == 0){
                //redis为空，从数据库查询，并将结果保存在数据库中
                System.out.println("redis为空，从数据库查询");
                //2.1调用service——>dao,查询数据
                List<SysMovie> data = sysMovieService.domesticBoxOfficeList();
                //查询并序列化为json字符串
                String json = new Gson().toJson(data);
                System.out.println("json:"+json);
                List<SysMovie> ps = data;
                //2.2将list序列化为json
                ObjectMapper mapper = new ObjectMapper();
                try {
                    domesticBoxOfficeListJson = mapper.writeValueAsString(ps);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                //2.3 将查询的结果，存入redis缓存
                jedis.set("domesticBoxOfficeList",domesticBoxOfficeListJson);
                //释放连接池资源关闭
                jedis.close();

                System.out.println("json:"+json);
                return getResult(data);

            }else{
                System.out.println("redis成功...");
                System.out.println("domesticBoxOfficeListJson："+domesticBoxOfficeListJson);
                Gson gson = new Gson();
//            解析json数据
                List<SysMovie> data = JSON.parseArray(domesticBoxOfficeListJson, SysMovie.class);
//            for(SysMovie u :data){
//                System.out.println(u.toString());
//            }
                return getResult(data);
            }

        }else if(listId == 3){
//            "foreignBoxOfficeList";
            //        2判断 redis的缓存数据 数据是否为null
            if(foreignBoxOfficeListJson == null || foreignBoxOfficeListJson.length() == 0){
                //redis为空，从数据库查询，并将结果保存在数据库中
                System.out.println("redis为空，从数据库查询");
                //2.1调用service——>dao,查询数据
                List<SysMovie> data = sysMovieService.foreignBoxOfficeList();
                //查询并序列化为json字符串
                String json = new Gson().toJson(data);
                System.out.println("json:"+json);
                List<SysMovie> ps = data;
                //2.2将list序列化为json
                ObjectMapper mapper = new ObjectMapper();
                try {
                    foreignBoxOfficeListJson = mapper.writeValueAsString(ps);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                //2.3 将查询的结果，存入redis缓存
                jedis.set("foreignBoxOfficeList",foreignBoxOfficeListJson);
                //释放连接池资源关闭
                jedis.close();

                System.out.println("json:"+json);
                return getResult(data);

            }else{
                System.out.println("redis成功...");
                System.out.println("foreignBoxOfficeListJson："+foreignBoxOfficeListJson);
                Gson gson = new Gson();
//            解析json数据
                List<SysMovie> data = JSON.parseArray(foreignBoxOfficeListJson, SysMovie.class);
//            for(SysMovie u :data){
//                System.out.println(u.toString());
//            }
                return getResult(data);
            }
        }else{
            //暂时只支持4种榜单
            return ResponseResult.error("抱歉，暂时只支持3种榜单，id为[1,3]");
        }







    }

}




//
////    这是测试代码
//    public static void main(String[] args) {
//        String businessTypeJson = "[{\"movieId\":1,\"movieName\":\"送你一朵小红花\",\"movieLength\":128,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/4425e22bc1264164a2fbdd01d56cd939.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Jun 4, 2021, 12:00:00 AM\",\"movieBoxOffice\":1258.0,\"movieIntroduction\":\"两个抗癌家庭，两组生活轨迹。影片讲述了一个温情的现实故事，思考和直面了每一个普通人都会面临的终极问题——想象死亡随时可能到来，我们唯一要做的就是爱和珍惜。\",\"moviePictures\":\"[\\\"/images/movie/2020/12/15/2886b23c40ba4c57829c8938b10aeedb.jpg\\\", \\\"/images/movie/2020/12/15/00733b7ceb284b26ac43fa93f20fd991.jpg\\\", \\\"/images/movie/2020/12/15/208998f6cd4e410f85db05631faca4c4.jpg\\\", \\\"/images/movie/2020/12/15/16fabc3258a843f28beccab37041fa80.jpg\\\"]\",\"movieCategoryList\":[{\"movieCategoryId\":1,\"movieCategoryName\":\"爱情\"},{\"movieCategoryId\":2,\"movieCategoryName\":\"喜剧\"},{\"movieCategoryId\":3,\"movieCategoryName\":\"动画\"},{\"movieCategoryId\":4,\"movieCategoryName\":\"剧情\"}]},{\"movieId\":3,\"movieName\":\"金刚川\",\"movieLength\":122,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/cd079baa53ab4caa8bf7e984ff59b11c.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Jun 3, 2021, 2:43:59 PM\",\"movieBoxOffice\":792.0,\"movieIntroduction\":\"1953年，抗美援朝战争进入最终阶段，志愿军在金城发动最后一场大型战役。为在指定时间到达，向 金城前线投放更多战力，志愿军战士们在物资匮乏、武装悬殊的情况下，不断抵御敌机狂轰滥炸，以血肉之躯一次次修补战火中的木桥。一段鲜为人知的历史，在暗流涌动的金刚川上徐徐展开......\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":1,\"movieCategoryName\":\"爱情\"},{\"movieCategoryId\":6,\"movieCategoryName\":\"动作\"}]},{\"movieId\":5,\"movieName\":\"赤狐书生\",\"movieLength\":125,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/94baa78550e34794823db457c27870a6.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Apr 5, 2021, 5:07:01 PM\",\"movieBoxOffice\":170.0,\"movieIntroduction\":\"清贫书生王子进（陈立农 饰）进京赶考，被来到人界取丹的狐妖白十三（李现 饰）盯上。为了骗取书生信任，狐妖联合各路妖鬼，设下重重陷阱。一场奇幻旅程等待着他们……\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":3,\"movieCategoryName\":\"动画\"},{\"movieCategoryId\":4,\"movieCategoryName\":\"剧情\"},{\"movieCategoryId\":13,\"movieCategoryName\":\"奇幻\"}]},{\"movieId\":2,\"movieName\":\"如果声音不记得\",\"movieLength\":102,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/9fc80b23da4847cc8a98c65abb14148f.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Jun 1, 2021, 2:43:49 PM\",\"movieBoxOffice\":74.0,\"movieIntroduction\":\"如果你喜欢的女孩，得了抑郁症，你该怎么办？辛唐（孙晨竣 饰）拥有通过声音给他人制造快乐的能力，但对同一人使用三次后，性命就会和此人绑定，只有对方开心，辛唐才能活命。偶然，辛唐救下准备自杀的同校网络红人吉择（章若楠 饰），两人借此绑定。吉择表面开朗，但实际患了抑郁症。辛唐最初为了活下去，费尽心思让吉择开心，而后续也真的投入深情。遗憾辛唐的秘密总会败露，而吉择暗黑的过往也在网络上被人揭开....愿爱情的温暖，能治愈抑郁的青春。\",\"moviePictures\":\"[\\\"/images/movie/2020/12/15/75caecc71ba345a690e1e2c661ea7f3a.jpg\\\"]\",\"movieCategoryList\":[{\"movieCategoryId\":4,\"movieCategoryName\":\"剧情\"},{\"movieCategoryId\":8,\"movieCategoryName\":\"悬疑\"},{\"movieCategoryId\":9,\"movieCategoryName\":\"惊悚\"}]},{\"movieId\":4,\"movieName\":\"沐浴之王\",\"movieLength\":103,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/7d886589922f45fbb4b9bfdc20a62a91.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Jun 1, 2021, 2:45:36 PM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"在一次搓澡服务中，富二代肖翔（彭昱畅 饰）和搓澡工周东海（乔杉 饰）发生矛盾，让周东海面临生活困境。肖翔因跳伞事故被送到医院记忆全失，周东海恰巧撞见，心生一计，骗肖翔是自己的弟弟并骗回周家澡堂当搓澡工，于是一个富二代开始了一段终身难忘的搓澡生涯……\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":1,\"movieCategoryName\":\"爱情\"},{\"movieCategoryId\":3,\"movieCategoryName\":\"动画\"},{\"movieCategoryId\":8,\"movieCategoryName\":\"悬疑\"}]},{\"movieId\":6,\"movieName\":\" 我和我的家乡\",\"movieLength\":153,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/5c6e503dc6154a9ab120256796acba34.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Jun 1, 2021, 2:45:48 PM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"电影《我和我的家乡》定档2020年国庆，延续《我和我的祖国》集体创作的方式，由张艺谋担当总监制，宁浩担任总导演，张一白担任总策划，宁浩、徐峥、陈思诚、闫非\\u0026彭大魔、邓超\\u0026俞白眉分别执导五个故事。\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":7,\"movieCategoryName\":\"科幻\"},{\"movieCategoryId\":13,\"movieCategoryName\":\"奇幻\"},{\"movieCategoryId\":19,\"movieCategoryName\":\"历史\"}]},{\"movieId\":7,\"movieName\":\"棒！少年\",\"movieLength\":108,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/ad8e89daada24ab9b76b972894fa4418.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Dec 11, 2020, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"一群来自全国各地的困境少年，被选进北京市郊一个爱心棒球基地，组成了一支特殊的棒球队，跟着70岁传奇教练“师爷”从零开始学习打棒球。少年马虎以“刺头”状态闯进了基地，天天打架干仗；而“元老球员”小双心思细腻敏感，对未来满是怀疑和不确定。不同家庭背景和心性习惯的少年在一起训练、生活，一支棒球棍，把他们带进无尽的冲突和欢乐，也带给他们久违的热血与梦想。几个月后，他们将飞往美国，代表中国登上世界少棒的顶级赛场，但基地的球场和宿舍却面临拆迁……少年们能否逆风挥棒，叫板自己的命运？\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":11,\"movieCategoryName\":\"冒险\"},{\"movieCategoryId\":14,\"movieCategoryName\":\"运动\"}]},{\"movieId\":8,\"movieName\":\"唐人街探案3\",\"movieLength\":136,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/3c856a4e413b4dd89cfd41a0d71faea9.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Feb 12, 2021, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"继曼谷、纽约之后，东京再出大案。唐人街神探唐仁（王宝强 饰）、秦风（刘昊然 饰）受侦探野田昊（妻夫木聪 饰）的邀请前往破案。“CRIMASTER世界侦探排行榜”中的侦探们闻讯后也齐聚东京，加入挑战，而排名第一Q的现身，让这个大案更加扑朔迷离，一场亚洲最强神探之间的较量即将爆笑展开……\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":2,\"movieCategoryName\":\"喜剧\"},{\"movieCategoryId\":6,\"movieCategoryName\":\"动作\"},{\"movieCategoryId\":10,\"movieCategoryName\":\"犯罪\"}]},{\"movieId\":9,\"movieName\":\"温暖的抱抱\",\"movieLength\":112,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/a0dc7fecccf2483391ee08070cd66832.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Dec 31, 2020, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"对整洁和计划有着超乎常人执念的鲍抱(常远 饰)，本以为自己是一个友情爱情的绝缘体，但在遇到个性率真宋温暖(李沁 饰) 、妙手“神经”贾医生(沈腾 饰)和假仁假义王为仁（乔杉 饰）之后，上演了一段阴差阳错的喜剧故事……\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":2,\"movieCategoryName\":\"喜剧\"},{\"movieCategoryId\":4,\"movieCategoryName\":\"剧情\"}]},{\"movieId\":10,\"movieName\":\"阳光劫匪\",\"movieLength\":104,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/69aec299204e46bcb9174d80e30961fd.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Dec 31, 2020, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"电影《阳光劫匪》讲述了晓雪（宋佳 饰）在寻找丢失的“爱女”途中，偶然遇到了开宠物店的阳光（马丽 饰），晓雪央求阳光一行人帮助寻找爱女娜娜。但娜娜竟然是只老虎，被富豪刘神奇（曾志伟 饰）绑架。阳光决定帮晓雪抢回娜娜，一行人开始了一场惊心动魄又令人捧腹的冒险之旅……\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":5,\"movieCategoryName\":\"恐怖\"},{\"movieCategoryId\":6,\"movieCategoryName\":\"动作\"}]},{\"movieId\":11,\"movieName\":\"神奇女侠1984\",\"movieLength\":151,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/d40ac8629a9b47629f927454a0c345e9.jpg\\\"]\",\"movieArea\":\"美国\",\"releaseDate\":\"Dec 18, 2020, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"神奇女侠的全新大银幕冒险来到了1980年代。神奇女侠戴安娜在华盛顿的自然历史博物馆过着与普通人无异的生活，然而在阻止了一场看似平常的劫案后，身边的一切都发生了变化。在强大的神力诱惑下，两位全新劲敌悄然出现——与神奇女侠“相爱相杀”的顶级掠食者豹女，以及掌控着能改变世界力量的麦克斯·洛德，一场惊天大战在所难免。另外一边，旧爱史蒂夫突然“死而复生”，与戴安娜再续前缘，然而浪漫感动之余，史蒂夫的回归也疑窦丛生。\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":6,\"movieCategoryName\":\"动作\"},{\"movieCategoryId\":7,\"movieCategoryName\":\"科幻\"}]},{\"movieId\":12,\"movieName\":\"摔跤吧！爸爸\",\"movieLength\":161,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/2ba31ac24b334fef80a4555f7cef5298.jpg\\\"]\",\"movieArea\":\"印度\",\"releaseDate\":\"May 5, 2017, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"这是一个温暖幽默的励志故事。马哈维亚 辛格·珀尕（阿米尔汗 饰）曾是印度国家摔跤冠军，因生活所迫放弃摔跤。他希望让儿子可以帮他完成梦想：赢得世界级金牌。结果生了四个女儿。本以为梦想就此破碎的辛格却意外发现女儿身上的惊人天赋，看到冠军希望的他决定不能让女儿的天赋浪费，像其他女孩一样只能洗衣做饭过一生，再三考虑之后，与妻子约定一年时间按照摔跤手的标准训练两个女儿：换掉裙子 、剪掉了长发，让她们练习摔跤，并赢得一个又一个冠军，最终赢来了成为榜样激励千千万万女性的机会……\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":2,\"movieCategoryName\":\"喜剧\"},{\"movieCategoryId\":15,\"movieCategoryName\":\"家庭\"}]},{\"movieId\":13,\"movieName\":\"我和我的祖国\",\"movieLength\":155,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/8341c3cdce0840c7b14dc88ce4dc5e0a.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Sep 30, 2019, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"七位导演分别取材新中国成立70周年以来，祖国经历的无数个历史性经典瞬间。讲述普通人与国家之间息息相关密不可分的动人故事。聚焦大时代大事件下，小人物和国家之间，看似遥远实则密切的关联，唤醒全球华人共同回忆。\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":23,\"movieCategoryName\":\"短片\"},{\"movieCategoryId\":24,\"movieCategoryName\":\"纪录片\"}]},{\"movieId\":14,\"movieName\":\"战狼2\",\"movieLength\":123,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/b410a941952d4612b07f18b0622bc73e.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Jul 27, 2017, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"故事发生在非洲附近的大海上，主人公冷锋遭遇人生滑铁卢，被“开除军籍”，本想漂泊一生的他，正当他打算这么做的时候，一场突如其来的意外打破了他的计划，突然被卷入了一场非洲国家叛乱，本可以安全撤离，却因无法忘记曾经为军人的使命，孤身犯险冲回沦陷区，带领身陷屠杀中的同胞和难民，展开生死逃亡。随着斗争的持续，体内的狼性逐渐复苏，最终孤身闯入战乱区域，为同胞而战斗。\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":6,\"movieCategoryName\":\"动作\"},{\"movieCategoryId\":10,\"movieCategoryName\":\"犯罪\"},{\"movieCategoryId\":11,\"movieCategoryName\":\"冒险\"}]},{\"movieId\":15,\"movieName\":\" 速度与激情7\",\"movieLength\":137,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/da999ff2914447e38cc854b1294d6c2a.jpg\\\"]\",\"movieArea\":\"美国\",\"releaseDate\":\"Apr 12, 2015, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"经历了紧张刺激的伦敦大战，多米尼克·托雷托（范·迪塞尔 饰）和他的伙伴们重新回归平静的生活，但是江湖的恩恩怨怨却决不允许他们轻易抽身而去。棘手的死对头欧文·肖瘫在医院，不得动弹，他的哥哥戴克·肖（杰森·斯坦森 饰）则发誓要为弟弟复仇。戴克曾是美国特种部队的王牌杀手，不仅身怀绝技，而且心狠手辣。他干掉了远在东京的韩，还几乎把探长卢克·霍布斯（道恩·强森 饰）送到另一个世界，甚至多米尼克那世外桃源般的家也被对方炸毁。复仇的利刃已经架在脖子上，多米尼克再也没有选择，他找到长久以来最为信赖的团队，与来势汹汹的戴克展开生死对决……\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":3,\"movieCategoryName\":\"动画\"},{\"movieCategoryId\":6,\"movieCategoryName\":\"动作\"}]},{\"movieId\":16,\"movieName\":\"哪吒之魔童降世\",\"movieLength\":110,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/2f69dce23efe4db99e189ca4886bed48.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Jul 26, 2019, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"天地灵气孕育出一颗能量巨大的混元珠，元始天尊将混元珠提炼成灵珠和魔丸，灵珠投胎为人，助周伐纣时可堪大用；而魔丸则会诞出魔王，为祸人间。元始天尊启动了天劫咒语，3年后天雷将会降临，摧毁魔丸。太乙受命将灵珠托生于陈塘关李靖家的儿子哪吒身上。然而阴差阳错，灵珠和魔丸竟然被掉包。本应是灵珠英雄的哪吒却成了混世大魔王。调皮捣蛋顽劣不堪的哪吒却徒有一颗做英雄的心。然而面对众人对魔丸的误解和即将来临的天雷的降临，哪吒是否命中注定会立地成魔？他将何去何从？\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":2,\"movieCategoryName\":\"喜剧\"},{\"movieCategoryId\":3,\"movieCategoryName\":\"动画\"},{\"movieCategoryId\":6,\"movieCategoryName\":\"动作\"},{\"movieCategoryId\":10,\"movieCategoryName\":\"犯罪\"}]},{\"movieId\":17,\"movieName\":\"寻梦环游记\",\"movieLength\":105,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/0b5191875a844e27bd52caf7048b354d.jpg\\\"]\",\"movieArea\":\"美国\",\"releaseDate\":\"Nov 24, 2017, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"热爱音乐的米格尔（安东尼·冈萨雷兹 Anthony Gonzalez 配音）不幸地出生在一个视音乐为洪水猛兽的大家庭之中，一家人只盼着米格尔快快长大，好继承家里传承了数代的制鞋产业。一年一度的亡灵节即将来临，每逢这一天，去世的亲人们的魂魄便可凭借着摆在祭坛上的照片返回现世和生者团圆。 在一场意外中，米格尔竟然穿越到了亡灵国度之中，在太阳升起之前，他必须得到一位亲人的祝福，否则就将会永远地留在这个世界里。米格尔决定去寻找已故的歌神德拉库斯（本杰明·布拉特 Benjamin Bratt 配音），因为他很有可能就是自己的祖父。途中，米格尔邂逅了落魄乐手埃克托（盖尔·加西亚·贝纳尔 Gael García Bernal 配音），也渐渐发现了德拉库斯隐藏已久的秘密。\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":3,\"movieCategoryName\":\"动画\"},{\"movieCategoryId\":4,\"movieCategoryName\":\"剧情\"}]},{\"movieId\":18,\"movieName\":\"我不是药神\",\"movieLength\":117,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/5711f4fa70ec4d53b714af51b2dc1819.jpg\\\"]\",\"movieArea\":\"中国大陆\",\"releaseDate\":\"Jul 5, 2018, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"一位不速之客的意外到访，打破了神油店老板程勇（徐峥 饰）的平凡人生，他从一个交不起房租的男性保健品商贩，一跃成为印度仿制药“格列宁”的独家代理商。收获巨额利润的他，生活剧烈变化，被病患们冠以“药神”的称号。但是，一场关于救赎的拉锯战也在波涛暗涌中慢慢展开......\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":2,\"movieCategoryName\":\"喜剧\"},{\"movieCategoryId\":6,\"movieCategoryName\":\"动作\"},{\"movieCategoryId\":7,\"movieCategoryName\":\"科幻\"}]},{\"movieId\":19,\"movieName\":\"火力全开\",\"movieLength\":85,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/e29341a6bf4640f7a6fb69442cade734.jpg\\\"]\",\"movieArea\":\"中国台湾\",\"releaseDate\":\"Oct 20, 2017, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"《火力全开》主要描述了王力宏火力全开演唱会从台前幕后准备到成型再到演出的全纪实，并且在其中穿插很多他从小到大的励志往事。《火力全开》记录的不只是场表演，更记录了这场演唱会从筹备到完成的思想和精神。这部电影忠实记录华语流行演唱会在现今的状态，并呈现出王力宏的音乐创作概念与历程、舞台下平易近人又生活化的一面，以及对梦想的坚持和发光。无论观众是不是王力宏的歌迷，都能从中一窥音乐之于世界的力量，成为东西方文化交流的种子。\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":4,\"movieCategoryName\":\"剧情\"}]},{\"movieId\":20,\"movieName\":\"疯狂动物城\",\"movieLength\":109,\"moviePoster\":\"[\\\"/images/movie/2020/12/15/190e45b2f62e414aae5f25c54df23c6d.jpg\\\"]\",\"movieArea\":\"美国\",\"releaseDate\":\"Mar 4, 2016, 12:00:00 AM\",\"movieBoxOffice\":0.0,\"movieIntroduction\":\"故事发生在一个所有哺乳类动物和谐共存的美好世界中，兔子朱迪（金妮弗·古德温 Ginnifer Goodwin 配音）从小就梦想着能够成为一名惩恶扬善的刑警，凭借着智慧和努力，朱迪成功的从警校中毕业进入了疯狂动物城警察局，殊不知这里是大型肉食类动物的领地，作为第一只，也是唯一的小型食草类动物，朱迪会遇到怎样的故事呢？ 近日里，城中接连发生动物失踪案件，就在全部警员都致力于调查案件真相之时，朱迪却被局长（伊德瑞斯·艾尔巴 Idris Elba 配音）发配成为了一名无足轻重的交警。某日，正在执勤的兔子遇见了名为尼克（杰森·贝特曼 Jason Bateman 配音）的狐狸，两人不打不相识，之后又误打误撞的接受了寻找失踪的水獭先生的任务，如果不能在两天之内找到水獭先生，朱迪就必须自愿离开警局。朱迪找到了尼克，两人联手揭露了一个隐藏在疯狂动物城之中的惊天秘密。\",\"moviePictures\":\"[]\",\"movieCategoryList\":[{\"movieCategoryId\":1,\"movieCategoryName\":\"爱情\"},{\"movieCategoryId\":2,\"movieCategoryName\":\"喜剧\"},{\"movieCategoryId\":3,\"movieCategoryName\":\"动画\"},{\"movieCategoryId\":8,\"movieCategoryName\":\"悬疑\"},{\"movieCategoryId\":9,\"movieCategoryName\":\"惊悚\"},{\"movieCategoryId\":10,\"movieCategoryName\":\"犯罪\"}]}]\n";
//                    Gson gson = new Gson();
//    List<SysMovie>  userList = fromToJson(businessTypeJson,new TypeToken<List<SysMovie>>(){}.getType());
//            for(SysMovie u :userList){
//                System.out.println(u.toString());
//            }
////        System.out.println( getResult(userList));
//    }
//
//    //根据泛型返回解析制定的类型
//    public static  <T> T fromToJson(String json, Type listType){
//        Gson gson = new Gson();
//        T t = null;
//        try {
//            t = gson.fromJson(json,listType);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return t;
//    }
