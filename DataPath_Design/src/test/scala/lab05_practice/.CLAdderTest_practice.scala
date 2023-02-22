// in ./src/tets/scala/lab05_practice/CLAdderTest_practice.scala
package lab05_practice

import chisel3.iotesters.{PeekPokeTester,Driver}

class CLAdderTest_practice (cl:CLAdder_practice) extends PeekPokeTester(cl){
    for (i <- 0 to 15){
        printf("i = %d", i)
        for (j <- 0 to 15){
            poke(cl.io.in1,i)
            poke(cl.io.in2,j)
            if (peek(cl.io.Cout)*16+peek(cl.io.Sum)!= (i+j)){
                println("oh NO!!!")
            }
        }
    }
    println("CLAdderTest completed !!!")

}

// todo : 入口函式
// todo : 格式 :
// object {test name} extends App{
//     Driver.execute(args, ()=> new {Module name}){
//         c => new {test name(c)}
//     }
// }
object CLAdderTest_practice extends App{
    Driver.execute(args, () => new CLAdder_practice){
        c => new CLAdderTest_practice(c)
    }
}