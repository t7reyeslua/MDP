package tudelft.mdp.backend.cron;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.enums.MessagesProtocol;
import tudelft.mdp.backend.endpoints.MessagingEndpoint;

@SuppressWarnings("serial")
public class KeepAliveGcmCronServlet extends HttpServlet {

    private static final Logger _logger = Logger.getLogger(KeepAliveGcmCronServlet.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
            _logger.info("KeepAliveGCM Cron Job has been executed at: " + Utils.getCurrentTimestamp());

            MessagingEndpoint messagingEndpoint = new MessagingEndpoint();
            messagingEndpoint.sendMessage(MessagesProtocol.SENDGCM_CMD_KEEPGCMALIVE + "|" + MessagesProtocol.KEEPGCMALIVE);
            //Put your logic here
            //BEGIN
            //END
        } catch (Exception ex) {
            _logger.severe("Oops: " + ex.getMessage());
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}