importPackage(com.monke.monkeybook.test);
var callback = {
    do1: function () {
        print("jsssssssssssss running1");
    },
    do2: function () {
         print("jsssssssssssss running2");
    }
}
var t = new Test(callback);
t.do1();
t.do2();