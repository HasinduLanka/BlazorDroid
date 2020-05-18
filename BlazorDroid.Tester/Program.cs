using System;
using System.IO;

namespace BlazorDroid.Tester
{
    class Program
    {
        static void Main()
        {
            Console.WriteLine("Hello World!");


            SimpleHTTPServer svr = new SimpleHTTPServer(Path.GetFullPath("wwwroot"), 6961);
            Console.ReadLine();
        }
    }
}
