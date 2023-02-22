// in ./src/test/scala/lab05_practice/FullAdderTest.scala
package lab05_practice

import chisel3.iotesters.{PeekPokeTester,Driver}

class FullAdderTest_practice(fa : FullAdder_practice) extends PeekPokeTester(fa){
    for (a <- 0 until 2){
        for (b <- 0 until 2){
            for (c <- 0 until 2){
                poke(fa.io.A, a)
                printf("a = %d\n", a)
                poke(fa.io.B, b)
                poke(fa.io.Cin, c)

                var x =  c & (a ^ b) 
                var y = a & b 

                expect(fa.io.Sum, (a^b^c))
                expect(fa.io.Cout, (x | y))

                step(1)
            }
        }
    }
    println("FullAdder_practice test completed!!!")
}

// todo : 準備入口函式
object FullAdderTest_practice extends App{
    Driver.execute(Array("-td", "./generated", "-tbn", "verilator"), () => new FullAdder_practice()){
        c => new FullAdderTest_practice(c)
    }
}