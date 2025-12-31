package net.flowclient.script;

import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ScriptManager {
    public static final File SCRIPTS_DIR = new File(MinecraftClient.getInstance().runDirectory, "flow_config/scripts");

    public static void init(){
        if(!SCRIPTS_DIR.exists()){
            SCRIPTS_DIR.mkdirs();
        }
    }

    public static void saveScript(String fileName, String content){
        init(); // 念の為フォルダがあるか確認
        File file = new File(SCRIPTS_DIR, fileName);
        try{
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            System.out.println("Saved script: " + file.getAbsolutePath());
        } catch(IOException e){
            e.printStackTrace();
            System.err.println("Failed to save script: " + fileName);
        }
    }

    public static String loadScript(String fileName){
        File file = new File(SCRIPTS_DIR, fileName);
        if(!file.exists()) return "";

        try{
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch(IOException e){
            e.printStackTrace();
            return "";
        }
    }

    public static List<String> getScriptFiles(){
        init();
        List<String> names = new ArrayList<>();
        File[] files = SCRIPTS_DIR.listFiles((dir, name) -> name.endsWith(".flow"));
        if(files != null){
            for(File f : files){
                names.add(f.getName());
            }
        }
        return names;
    }

    public static boolean renameScript(String oldName, String newName){
        init();
        File oldFile = new File(SCRIPTS_DIR, oldName);
        File newFile = new File(SCRIPTS_DIR, newName);
        if(!oldFile.exists() || newFile.exists()){
            return false;
        }
        try{
            Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }
}
