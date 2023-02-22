// in  ./src/test/scala/lab05_practice/RCAdderTest_practice.scala
package lab05_practice

import chisel3.iotesters.{PeekPokeTester, Driver}

class RCAdderTest_practice (dut:RCAdder_practice) extends PeekPokeTester(dut){
    
    val in1 = Array(5,32,1,77,34,55,12)
    val in2 = Array(3456,89489,78,5216,4744,8,321)

    // method 1
    //in1.zip(in2)
    (in1 zip in2).foreach{
        case(i, j)=>
            poke(dut.io.In1, i)
            poke(dut.io.In2, j)
            expect(dut.io.Sum, i+j)
            step(1)
    }

    // method 2
    // for (i <- in1){
    //     for (j <- in2){
    //         poke(dut.io.In1, i)
    //         poke(dut.io.In2, j)
    //         expect(dut.io.Sum, i+j)
    //     }
    // }

    println("RCAdderTest_practice completed !!!")

    // todo : test入口函式

    object RCAdderTest_practice extends App{
        Driver.execute(args, ()=>new RCAdder_practice(32)){
            c => new RCAdderTest_practice(c)
        }
    }

}