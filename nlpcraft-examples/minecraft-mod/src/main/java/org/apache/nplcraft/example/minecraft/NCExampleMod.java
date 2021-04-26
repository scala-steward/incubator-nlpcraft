/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.nplcraft.example.minecraft;

import com.google.gson.Gson;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FileUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@Mod("nlpcraft_mod")
public class NCExampleMod {
    private static final String DFLT_EMAIL = "admin@admin.com";
    private static final String DFLT_PSWD = "admin";
    private static final String DFLT_HOST = "0.0.0.0";
    private static final int DFLT_PORT = 8081;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MODEL_ID = "nlpcraft.minecraft.ex";
    private static final Gson GSON = new Gson();

    private final Set<String> convCmds = new HashSet<>();

    private NCSignIn creds;
    private String baseUrl;
    private MinecraftServer server;
    private String token;

    private static class AskParams {
        private String mdlId;
        private String acsTok;
        private String txt;
    }

    private static class NCResponse {
        private String status;
        private NCState state;
    }

    private static class NCState {
        private Integer errorCode;
        private String error;
        private String status;
        private String resBody;
    }

    private static class NCSignIn {
        private String email;
        private String passwd;
    }

    private static class NCSignResponse {
        private String acsTok;
    }

    private static class NCSettings {
        private String email;
        private String passwd;
        private String host;
        private int port;
    }

    private static class UnauthorizedException extends Exception {
        // No-op.
    }

    public NCExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        this.server = event.getServer();

        loadSettings();
    }

    @SubscribeEvent
    public void onCommandEvent(CommandEvent event) {
        String cmd = event.getParseResults().getReader().getString();

        // Converted command skipped.
        if (convCmds.remove(cmd)) {
            return;
        }

        try {
            String convCmd = '/' + askProbe(cmd).state.resBody;

            LOGGER.info("Command '{}' was converted to '{}'", cmd, convCmd);

            event.setCanceled(true);

            // This command should be skipped in this 'mod'.
            convCmds.add(convCmd);

            server.getCommandManager().handleCommand(server.getCommandSource(), convCmd);
        }
        catch (Exception e) {
            LOGGER.error("Execution command unexpected error [cmd=" + cmd + ']', e);
        }
    }

    private static NCSignIn mkSignin(String email, String passwd) {
        NCSignIn s = new NCSignIn();

        s.email = email;
        s.passwd = passwd;

        return s;
    }

    private NCResponse askProbe(String txt) throws Exception {
        assert baseUrl != null;

        AskParams params = new AskParams();

        params.mdlId = MODEL_ID;
        params.txt = txt.startsWith("/") ? txt.substring(1) : txt;

        if (token == null) {
            this.token = signin();
        }

        params.acsTok = this.token;

        NCResponse resp;

        try {
            resp = post("ask/sync", GSON.toJson(params), NCResponse.class);
        }
        catch (UnauthorizedException e) {
            // Token can be expired.
            this.token = signin();

            params.acsTok = this.token;

            resp = post("ask/sync", GSON.toJson(params), NCResponse.class);
        }

        if (resp.state.error != null) {
            throw new Exception("Invalid response [error=" + resp.state.error + ']');
        }
        else if (resp.state.resBody == null) {
            throw new Exception("Invalid empty response");
        }

        return resp;
    }

    private String signin() throws Exception {
        return post("signin", GSON.toJson(creds), NCSignResponse.class).acsTok;
    }

    private <T> T post(String url, String postJson, Class<T> clazz) throws Exception {
        assert baseUrl != null;

        HttpURLConnection conn = (HttpURLConnection) new URL(baseUrl + url).openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setConnectTimeout(1_000);
        conn.setReadTimeout(5_000);

        conn.setDoOutput(true);

        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.writeBytes(postJson);

            out.flush();
        }

        int code = conn.getResponseCode();

        if (code == 401) {
            throw new UnauthorizedException();
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return GSON.fromJson(in, clazz);
        }
    }

    private void loadSettings() {
        try (
            Reader reader =
                Files.newBufferedReader(
                    FileUtil.resolveResourcePath(Paths.get("config"), "nlpcraft-settings", ".json")
                )
        ) {
            NCSettings settings = GSON.fromJson(reader, NCSettings.class);

            LOGGER.info("Credentials file read.");

            this.creds = mkSignin(settings.email, settings.passwd);
            this.baseUrl = "http://" + settings.host + ":" + settings.port + "/api/v1/";

            return;
        }
        catch (NoSuchFileException e) {
            LOGGER.info("Credentials were not found, default configuration used.");
        }
        catch (Exception e) {
            LOGGER.error("Setting loading unexpected error", e);
        }

        this.creds = mkSignin(DFLT_EMAIL, DFLT_PSWD);
        this.baseUrl = "http://" + DFLT_HOST + ":" + DFLT_PORT + "/api/v1/";
    }
}