package com.kryang.lolibot.plugin;

import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotPlugin;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotBase;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessagePlugin extends BotPlugin {
    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event) {
        // 这里展示了event消息链的用法. List里面可能是 at -> text -> image -> face -> text 形式, 根据元素类型组成 List。
        // List 每一个元素 有type(String)和data(Map<String, String>)，type 表示元素是什么, data 表示元素的具体数据，如at qq，image url，face id
        List<OnebotBase.Message> messageChain = event.getMessageList();
        if (messageChain.size() > 0) {
            OnebotBase.Message message = messageChain.get(0);
            if (message.getType().equals("text")) {
                String text = message.getDataMap().get("text");
                if ("涩图".equals(text) || "色图".equals(text)) {
                    Msg msg = Msg.builder()
                            .face(1)
                            .text("目前没有涩图。。/r/n")
                            .text("图片发送测试：")
                            .image("https://www.baidu.com/img/flexible/logo/pc/result@2.png");
                    bot.sendPrivateMsg(event.getUserId(), msg, false);
                }
            }
        }

        return MESSAGE_IGNORE;
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event) {
        // 这里展示了RawMessage的用法（纯String）
        long groupId = event.getGroupId();
        String text = event.getRawMessage();
        if ("hello".equals(text)) {
            bot.sendGroupMsg(groupId, "hi", false);
            return MESSAGE_BLOCK; // 当存在多个plugin时，不执行下一个plugin
        }
        if (text.contains("图")) {
            Msg msg = Msg.builder()
                    .face(1)
                    .text("目前没有涩图。。/r/n")
                    .text("图片发送测试：")
                    .image("https://www.baidu.com/img/flexible/logo/pc/result@2.png");
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return MESSAGE_IGNORE;
        }
        return MESSAGE_IGNORE; // 当存在多个plugin时，继续执行下一个plugin
    }
}
