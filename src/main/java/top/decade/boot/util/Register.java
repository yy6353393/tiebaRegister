package top.decade.boot.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.decade.boot.domain.Cookie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Register {

    /** 获取日志记录器对象 */
    private static final Logger LOGGER = LoggerFactory.getLogger(Register.class);

    /** 获取用户所有关注贴吧 */
    String LIKE_URL = "https://tieba.baidu.com/mo/q/newmoindex";
    /** 获取用户的tbs */
    String TBS_URL = "http://tieba.baidu.com/dc/common/tbs";
    /** 贴吧签到接口 */
    String SIGN_URL = "http://c.tieba.baidu.com/c/c/forum/sign";

    /** 存储用户所关注的贴吧 */
    private List<String> follow;
    /** 签到成功的贴吧列表 */
    private static List<String>  success;
    /** 用户的tbs */
    private String tbs = "";
    /** 用户所关注的贴吧数量 */
    private static Integer followNum = 201;

    public static void app(String bduss){

        Cookie cookie = Cookie.getInstance();
        cookie.setBDUSS(bduss);
        Register run = new Register();
        run.getTbs();
        run.getFollow();
        run.runSign();
        LOGGER.info("共 {} 个贴吧 - 成功: {} - 失败: {}",followNum,success.size(),followNum-success.size());
    }


    /**
     * 进行登录，获得 tbs ，签到的时候需要用到这个参数
     * @author decade
     * @Time 2020-10-31
     */
    public void getTbs(){
        try{
            JSONObject jsonObject = Request.get(TBS_URL);
            if("1".equals(jsonObject.getString("is_login"))){
                LOGGER.info("获取tbs成功");
                tbs = jsonObject.getString("tbs");
            } else{
                LOGGER.warn("获取tbs失败 -- " + jsonObject);
            }
        } catch (Exception e){
            LOGGER.error("获取tbs部分出现错误 -- " + e);
        }
    }

    /**
     * 获取用户所关注的贴吧列表
     * @author decade
     * @Time 2020-10-31
     */
    public void getFollow(){
        success = new ArrayList<>();
        try{
            JSONObject jsonObject = Request.get(LIKE_URL);
            LOGGER.info("获取贴吧列表成功");
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("like_forum");
            followNum = jsonArray.size();
            // 获取用户所有关注的贴吧
            for (Object array : jsonArray) {
                if("0".equals(((JSONObject) array).getString("is_sign"))){
                    // 将为签到的贴吧加入到 follow 中，待签到
                    follow.add(((JSONObject) array).getString("forum_name"));
                } else{
                    // 将已经成功签到的贴吧，加入到 success
                    success.add(((JSONObject) array).getString("forum_name"));
                }
            }
        } catch (Exception e){
            LOGGER.error("获取贴吧列表部分出现错误 -- " + e);
        }
    }

    /**
     * 开始进行签到，每一轮性将所有未签到的贴吧进行签到，一共进行5轮，如果还未签到完就立即结束
     * 一般一次只会有少数的贴吧未能完成签到，为了减少接口访问次数，每一轮签到完等待1分钟，如果在过程中所有贴吧签到完则结束。
     * @author decade
     * @Time 2020-10-31
     */
    public void runSign(){
        // 当执行 5 轮所有贴吧还未签到成功就结束操作
        Integer flag = 5;
        follow = new ArrayList<>();
        try{
            while(success.size()<followNum&&flag>0){
                LOGGER.info("-----第 {} 轮签到开始-----", 5 - flag + 1);
                LOGGER.info("还剩 {} 贴吧需要签到", followNum - success.size());
                Iterator<String> iterator = follow.iterator();
                while(iterator.hasNext()){
                    String s = iterator.next();
                    String body = "kw="+s+"&tbs="+tbs+"&sign="+ Encryption.enCodeMd5("kw="+s+"tbs="+tbs+"tiebaclient!!!");
                    JSONObject post = Request.post(SIGN_URL, body);
                    if("0".equals(post.getString("error_code"))){
                        iterator.remove();
                        success.add(s);
                        LOGGER.info(s + ": " + "签到成功");
                    } else {
                        LOGGER.warn(s + ": " + "签到失败");
                    }
                }
                if (success.size() != followNum){
                    // 为防止短时间内多次请求接口，触发风控，设置每一轮签到完等待 5 分钟
                    Thread.sleep(1000 * 60 * 5);
                    /**
                     * 重新获取 tbs
                     * 尝试解决以前第 1 次签到失败，剩余 4 次循环都会失败的错误。
                     */
                    getTbs();
                }
                flag--;
            }
        } catch (Exception e){
            LOGGER.error("签到部分出现错误 -- " + e);
        }
    }

}
