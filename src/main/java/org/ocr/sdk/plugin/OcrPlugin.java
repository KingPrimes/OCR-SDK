package org.ocr.sdk.plugin;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.ocr.sdk.utils.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Shiro
@Component
public class OcrPlugin {

    @GroupMessageHandler
    public void group(Bot bot, GroupMessageEvent event){
        ocrFun(bot, event.getGroupId(), null,event.getRawMessage());
    }

    @PrivateMessageHandler
    public void privy(Bot bot, PrivateMessageEvent event){
        ocrFun(bot,null, event.getUserId(), event.getRawMessage());
    }

    private static void ocrFun(Bot bot,Long groupId,Long userId,String msg){

        if(StringUtils.regex(msg,"^ocrT[\\s\\S]*")){
            if(CqMatcher.isCqImage(msg)){
                List<String> cqImageUrl = CqParse.build(msg).getCqImageUrl();
                try{
                    List<Point> points = Ocr.threadOcr(cqImageUrl.get(0));
                    MsgUtils msgs = MsgUtils.builder();
                    for (Point point : points) {
                        msgs.text(point.getMsg()+"\n");
                    }
                    if(groupId == null){
                        bot.sendPrivateMsg(userId,"识别结果如下：\n"+msgs.build(),false);
                        return;
                    }else{
                        bot.sendGroupMsg(groupId,"识别结果如下：\n"+msgs.build(),false);
                        return;
                    }
                }catch (Exception e){
                    if(groupId == null){
                        bot.sendGroupMsg(userId,"识别出错！错误信息：\n"+e.getMessage(),false);
                    }else{
                        bot.sendGroupMsg(groupId,"识别出错！错误信息：\n"+e.getMessage(),false);
                    }
                    e.printStackTrace();
                }
            }
        }

        if(StringUtils.regex(msg,"^ocr[\\s\\S]*")){
            if(CqMatcher.isCqImage(msg)){
                List<String> cqImageUrl = CqParse.build(msg).getCqImageUrl();
                try{
                    if(groupId == null){
                        bot.sendPrivateMsg(userId,"识别结果如下：\n"+Ocr.ocr(cqImageUrl.get(0)),false);
                    }else{
                        bot.sendGroupMsg(groupId,"识别结果如下：\n"+Ocr.ocr(cqImageUrl.get(0)),false);
                    }
                }catch (Exception e){
                    if(groupId == null){
                        bot.sendGroupMsg(userId,"识别出错！错误信息：\n"+e.getMessage(),false);
                    }else{
                        bot.sendGroupMsg(groupId,"识别出错！错误信息：\n"+e.getMessage(),false);
                    }
                    e.printStackTrace();
                }
            }
        }
    }
}
