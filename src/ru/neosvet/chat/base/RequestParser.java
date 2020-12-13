package ru.neosvet.chat.base;

import ru.neosvet.chat.base.requests.SampleRequest;
import ru.neosvet.chat.server.Server;

public class RequestParser {
    private String owner;
    private String recipient;
    private Request result;

    public RequestParser(String owner) {
        this.owner = owner;
    }

    public String getRecipient() {
        return recipient;
    }

    public Request getResult() {
        return result;
    }

    public boolean HasRecipient() {
        return recipient != null;
    }

    public boolean parse(String s) {
        recipient = null;
        result = null;

        if (!s.startsWith("/"))
            return false;

        try {
            String[] m = s.split(" ", 3);
            switch (m[0]) {
                case Cmd.MSG_PRIVATE:
                    result = RequestFactory.createPrivateMsg(owner, m[1], m[2]);
                    break;
                case Cmd.MSG_GLOBAL:
                    result = RequestFactory.createGlobalMsg(owner, m[1]);
                    break;
                case Cmd.EXIT:
                    result = RequestFactory.createExit();
                    break;
                case Cmd.LIST:
                    result = new SampleRequest(RequestType.LIST);
                    break;
                case Cmd.NICK:
                    result = RequestFactory.createNick(m[1]);
                    break;
                case Cmd.STOP:
                    if (!IsServer())
                        return false;
                    result = RequestFactory.createStop();
                    break;
                case Cmd.KICK:
                    if (!IsServer())
                        return false;
                    result = RequestFactory.createKick();
                    recipient = m[1];
                    break;
            }
            return result != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean IsServer() {
        return owner.equals(Server.NICK);
    }
}
