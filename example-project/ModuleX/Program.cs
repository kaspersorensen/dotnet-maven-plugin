using System;

namespace ModuleX
{
    public class Program
    {
        public string GetMessage()
        {
            return "Hello world";
        }

        public static void Main(string[] args)
        {
            var o = new Program();
            Console.WriteLine(o.GetMessage());
        }
    }
}
