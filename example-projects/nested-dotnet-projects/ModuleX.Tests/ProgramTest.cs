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
    }
}
