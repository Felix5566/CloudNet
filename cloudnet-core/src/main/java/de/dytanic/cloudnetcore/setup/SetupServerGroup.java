/*
 * Copyright (c) Tarek Hosni El Alaoui 2017
 */

package de.dytanic.cloudnetcore.setup;

import de.dytanic.cloudnet.command.CommandSender;
import de.dytanic.cloudnet.lib.NetworkUtils;
import de.dytanic.cloudnet.lib.server.ServerGroup;
import de.dytanic.cloudnet.lib.server.ServerGroupMode;
import de.dytanic.cloudnet.lib.server.ServerGroupType;
import de.dytanic.cloudnet.lib.server.advanced.AdvancedServerConfig;
import de.dytanic.cloudnet.lib.server.template.Template;
import de.dytanic.cloudnet.lib.server.template.TemplateResource;
import de.dytanic.cloudnet.setup.Setup;
import de.dytanic.cloudnet.setup.SetupRequest;
import de.dytanic.cloudnet.setup.SetupResponseType;
import de.dytanic.cloudnetcore.CloudNet;
import de.dytanic.cloudnetcore.network.components.Wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Tareko on 21.10.2017.
 */
public class SetupServerGroup {

    private String name;

    public SetupServerGroup(CommandSender commandSender, String name) {
        this.name = name;

        Setup setup = new Setup().setupCancel(() -> System.out.println("Setup cancelled!"))
                                 .setupComplete(data -> {
                                     // Make sure there is at least one valid wrapper
                                     List<String> wrappers = new ArrayList<>(Arrays.asList(data.getString("wrapper").split(",")));
                                     if (wrappers.size() == 0) {
                                         return;
                                     }
                                     for (short i = 0; i < wrappers.size(); i++) {
                                         if (!CloudNet.getInstance().getWrappers().containsKey(wrappers.get(i))) {
                                             wrappers.remove(wrappers.get(i));
                                         }
                                     }
                                     if (wrappers.size() == 0) {
                                         return;
                                     }

                                     ServerGroupMode serverGroupMode = ServerGroupMode.valueOf(data.getString("mode").toUpperCase());

                                     ServerGroupType serverGroupType = null;

                                     for (ServerGroupType serverGroup : ServerGroupType.values()) {
                                         if (serverGroup.name().equalsIgnoreCase(data.getString("type").toUpperCase())) {
                                             serverGroupType = serverGroup;
                                         }
                                     }
                                     if (serverGroupType == null) {
                                         serverGroupType = ServerGroupType.BUKKIT;
                                     }

                                     ServerGroup serverGroup = new ServerGroup(name,
                                                                               wrappers,
                                                                               serverGroupMode.equals(ServerGroupMode.LOBBY),
                                                                               data.getInt("memory"),
                                                                               data.getInt("memory"),
                                                                               0,
                                                                               true,
                                                                               data.getInt("startup"),
                                                                               data.getInt("onlineGlobal"),
                                                                               data.getInt("onlineGroup"),
                                                                               180,
                                                                               100,
                                                                               100,
                                                                               data.getInt("percent"),
                                                                               serverGroupType,
                                                                               serverGroupMode,
                                                                               Arrays.asList(new Template("default",
                                                                                                          TemplateResource.valueOf(data.getString(
                                                                                                              "template")),
                                                                                                          null,
                                                                                                          new String[0],
                                                                                                          new ArrayList<>())),
                                                                               new AdvancedServerConfig(false,
                                                                                                        false,
                                                                                                        false,
                                                                                                        !serverGroupMode.equals(
                                                                                                            ServerGroupMode.STATIC)));
                                     CloudNet.getInstance().getConfig().createGroup(serverGroup);
                                     CloudNet.getInstance().getServerGroups().put(serverGroup.getName(), serverGroup);
                                     CloudNet.getInstance().setupGroup(serverGroup);
                                     for (Wrapper wrapper : CloudNet.getInstance().toWrapperInstances(wrappers)) {
                                         wrapper.updateWrapper();
                                     }
                                     commandSender.sendMessage("The server group " + serverGroup.getName() + " is now created!");
                                 })
                                 .request(new SetupRequest("memory",
                                                           "How many MB RAM should the server group have?",
                                                           "Specified Memory is invalid",
                                                           SetupResponseType.NUMBER,
                                                           key -> NetworkUtils.checkIsNumber(key) && Integer.parseInt(key) > 64))
                                 .request(new SetupRequest("startup",
                                                           "How many servers should always be online?",
                                                           "Specified startup count is invalid",
                                                           SetupResponseType.NUMBER,
                                                           key -> true))
                                 .request(new SetupRequest("percent",
                                                           "How full does the server have to be until a new server is started? (In Percent)?",
                                                           "Specified percent count is invalid",
                                                           SetupResponseType.NUMBER,
                                                           key -> NetworkUtils.checkIsNumber(key) && Integer.parseInt(key) <= 100))
                                 .request(new SetupRequest("mode",
                                                           "Which server group mode should be used? [STATIC, STATIC_LOBBY, LOBBY, DYNAMIC]",
                                                           "Specified server group mode is invalid",
                                                           SetupResponseType.STRING,
                                                           key -> key.equalsIgnoreCase("STATIC") || key.equalsIgnoreCase(
                                                               "STATIC_LOBBY") || key.equalsIgnoreCase("LOBBY") || key.equalsIgnoreCase(
                                                               "DYNAMIC")))
                                 .request(new SetupRequest("type",
                                                           "Which servergroup type should be used? [BUKKIT, CAULDRON, GLOWSTONE]",
                                                           "Specified group type is invalid",
                                                           SetupResponseType.STRING,
                                                           key -> key.equals("BUKKIT") || key.equals("GLOWSTONE") || key.equals("CAULDRON")))
                                 .request(new SetupRequest("template",
                                                           "What is the backend of the group default template? [\"LOCAL\" for the wrapper local | \"MASTER\" for the master backend]",
                                                           "Specified string is invalid",
                                                           SetupResponseType.STRING,
                                                           key -> key.equals("MASTER") || key.equals("LOCAL")))
                                 .request(new SetupRequest("onlineGroup",
                                                           "How many servers should be online if 100 players are online in the group?",
                                                           "Specified string is invalid",
                                                           SetupResponseType.NUMBER,
                                                           null))
                                 .request(new SetupRequest("onlineGlobal",
                                                           "How many servers should be online if 100 global players are online?",
                                                           "Specified string is invalid",
                                                           SetupResponseType.NUMBER,
                                                           null))

                                 .request(
                                     new SetupRequest("wrapper",
                                                      "Which wrappers should be used for this group?",
                                                      "Specified string is invalid",
                                                      SetupResponseType.STRING,
                                                      key -> {
                                                          // Make sure there is at least one valid wrapper
                                                          List<String> wrappers = new ArrayList<>(Arrays.asList(key.split(",")));

                                                          if (wrappers.size() == 0) {
                                                              return false;
                                                          }
                                                          for (short i = 0; i < wrappers.size(); i++) {
                                                              if (!CloudNet.getInstance()
                                                                           .getWrappers()
                                                                           .containsKey(wrappers.get(i))) {
                                                                  wrappers.remove(wrappers.get(i));
                                                              }
                                                          }
                                                          return wrappers.size() != 0;
                                                      }));
        setup.start(CloudNet.getLogger().getReader());
    }

    public String getName() {
        return name;
    }
}
