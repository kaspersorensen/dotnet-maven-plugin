using System;
using Xunit;

namespace ModuleX
{
    public class ProgramTest
    {
        [Fact]
        public void TestGetMessage()
        {
            var o = new Program();
            Assert.Equal("Hello world", o.GetMessage());
        }

        [Fact]
        public void TestEnvironmentVariableSet()
        {
            // this will ensure the environment variable injection is working
            var envvar = Environment.GetEnvironmentVariable("THIS_IS_FROM_MAVEN");
            Assert.Equal("Hello dotnet", envvar);
        }
    }
}
