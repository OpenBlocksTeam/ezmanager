package com.openblocks.module.ezmanager;

import android.content.Context;

import com.openblocks.moduleinterface.OpenBlocksModule;
import com.openblocks.moduleinterface.exceptions.NotSupportedException;
import com.openblocks.moduleinterface.models.OpenBlocksFile;
import com.openblocks.moduleinterface.models.OpenBlocksRawProject;
import com.openblocks.moduleinterface.models.config.OpenBlocksConfig;
import com.openblocks.moduleinterface.models.config.OpenBlocksConfigItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * EzManager is a simple and easy manager, it stores the project files inside getFilesDir()/projects
 */
public class EzManager implements OpenBlocksModule.ProjectManager {

    WeakReference<Context> context;
    OpenBlocksConfig config;

    File files_dir;
    File projects_folder;

    @Override
    public Type getType() {
        return Type.PROJECT_MANAGER;
    }

    @Override
    public void initialize(Context context) {
        this.context = new WeakReference<>(context);
        files_dir = context.getFilesDir();
        projects_folder = new File(context.getFilesDir(), "projects");

        // Initialize our config
        config = new OpenBlocksConfig();
        config.addItem("", new OpenBlocksConfigItem("", "", "", OpenBlocksConfig.Type.INPUT_NUMBER));
    }


    @Override
    public OpenBlocksConfig setupConfig() {
        return config;
    }

    @Override
    public void applyConfig(OpenBlocksConfig openBlocksConfig) {
        this.config = openBlocksConfig;
    }


    @Override
    public void saveProject(OpenBlocksRawProject project) {

        // Simply save the project in it's raw form
        File project_folder = new File(files_dir, project.ID);

        // Create the folder for this project if it doesn't exists
        if (!project_folder.exists()) {
            project_folder.mkdir();
        }

        for (OpenBlocksFile file : project.files) {
            try {
                FileUtil.writeFile(new File(project_folder, file.name), file.data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public OpenBlocksRawProject getProject(String project_id) {
        // Simply read the files
        OpenBlocksRawProject project = new OpenBlocksRawProject();
        project.files = new ArrayList<>();

        File project_folder = new File(files_dir, project_id);

        if (!project_folder.exists())
            return null;

        for (File file : project_folder.listFiles()) {
            try {
                project.files.add(new OpenBlocksFile(FileUtil.readFile(file), file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return project;
    }

    @Override
    public ArrayList<OpenBlocksRawProject> listProjects() {
        ArrayList<OpenBlocksRawProject> projects = new ArrayList<>();

        File project_folder = projects_folder;

        if (!project_folder.exists())
            return null;

        for (File project : project_folder.listFiles()) {
            ArrayList<OpenBlocksFile> project_files = new ArrayList<>();

            for (File project_file : project.listFiles()) {
                try {
                    project_files.add(new OpenBlocksFile(FileUtil.readFile(project_file), project_file.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            projects.add(new OpenBlocksRawProject(project.getName(), project_files));
        }

        return projects;
    }


    @Override
    public OpenBlocksFile exportProject(OpenBlocksRawProject openBlocksRawProject) throws NotSupportedException {
        throw new NotSupportedException("Exporting project is not supported yet");
    }

    @Override
    public OpenBlocksRawProject importProject(OpenBlocksFile openBlocksFile) throws NotSupportedException {
        throw new NotSupportedException("Importing project is not supported yet");
    }


    private static class FileUtil {
        public static void writeFile(File file, byte[] data) throws IOException {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
        }

        public static byte[] readFile(File file) throws IOException {
            StringBuilder output = new StringBuilder();
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];

            int length;
            while ((length = fis.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toByteArray();
        }
    }
}
