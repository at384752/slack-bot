import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.model.Message;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        var app = new App();

        app.command("/quotecount", (req, ctx) -> {
            String userText = req.getPayload().getText();
            String channelId = req.getPayload().getChannelId();

            String userId = extractUserId(userText);
            if (userId == null) {
                return ctx.ack("Invalid user mentioned! Please use the format @username.");
            }

            int mentionCount = countUserMentionsInChannel(channelId, userId, ctx);

            return ctx.ack("User <@" + userId + "> has been quoted " + mentionCount + " times in this channel.");
        });

        new SocketModeApp(app).start();
    }

    private static String extractUserId(String text) {
        if (text.startsWith("<@") && text.endsWith(">")) {
            return text.substring(2, text.indexOf("|"));
        }
        return null;
    }

    private static int countUserMentionsInChannel(String channelId, String userId, SlashCommandContext ctx)
        throws IOException, SlackApiException {

    var client = ctx.client();
    var token = ctx.getBotToken();

    ConversationsHistoryResponse historyResponse = client.conversationsHistory(r -> r
            .token(token)
            .channel(channelId)
    );

    if (!historyResponse.isOk()) {
        System.out.println("Slack API error: " + historyResponse.getError());
        return 0;
    }

    List<Message> messages = historyResponse.getMessages();
    if (messages == null) {
        System.out.println("Messages list is null — Slack returned no data.");
        return 0;
    }

    int count = 0;
    for (Message message : messages) {
        System.out.println(message.getText());
        if (message.getText() != null && message.getText().contains("<@" + userId + ">")) {
            count++;
        }
    }
    return count;
}

}
