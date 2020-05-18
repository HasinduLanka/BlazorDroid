using Android.OS;
using BlazorDroid;
using System;
using System.IO;

public class ServerInvoker
{
    Android.Content.Res.AssetManager Assets;
    string basePath;
    public void Start(Android.Content.Res.AssetManager ass)
    {
        Assets = ass;
        string root = "Blazite/wwwroot/";
        basePath = System.Environment.GetFolderPath(System.Environment.SpecialFolder.LocalApplicationData);
        RecursiveCopy(root);
        string fp = Path.GetFullPath(basePath);
        SimpleHTTPServer svr = new SimpleHTTPServer(Path.Combine(basePath, root), 6961);
    }


    private void RecursiveCopy(string path)
    {
        string[] lst = Assets.List(path);

        foreach (string file in lst)
        {
            string filepath = Path.Combine(path, file);
            try
            {
                Stream src = Assets.Open(filepath);
                Stream dest = File.OpenWrite(Path.Combine(basePath, filepath));
                src.CopyTo(dest);
                src.Flush();
                dest.Close();
            }
            catch (Java.IO.FileNotFoundException)
            {
                Directory.CreateDirectory(Path.Combine(basePath, filepath));
                RecursiveCopy(filepath);
            }
        }


    }
}