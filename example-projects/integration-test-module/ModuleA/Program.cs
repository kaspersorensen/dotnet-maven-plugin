using System;

namespace ModuleA
{
    public class Program
    {
        public string GetMessage()
        {
            return "Hello A";
        }

        public static void Main(string[] args)
        {
            var o = new Program();
            Console.WriteLine(o.GetMessage());
        }
    }
}
