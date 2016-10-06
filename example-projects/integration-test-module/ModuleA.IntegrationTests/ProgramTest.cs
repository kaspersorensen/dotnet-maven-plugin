using System;
using Xunit;

namespace ModuleA
{
    public class ProgramTest
    {
        [Fact]
        public void TestGetMessage()
        {
            var o = new Program();
            Assert.Equal("Hello A", o.GetMessage());
        }
        
        [Fact]
        public void TestGetMessageWillFail()
        {
            var o = new Program();
            Assert.Equal("Hello B", o.GetMessage());
        }
    }
}
