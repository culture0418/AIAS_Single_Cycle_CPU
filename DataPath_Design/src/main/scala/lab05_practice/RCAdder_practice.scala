// in ./src/main/scala/lab05_practice/RCAdder_practice.scala

package lab05_practice

import chisel3._ 
import chisel3.util._ 
import chisel3.stage.ChiselStage

class RCAdder_practice (n: Int) extends Module{
    val io = IO(new Bundle{
        val Cin = Input(UInt(1.W))
        val In1 = Input(UInt(n.W))
        val In2 = Input(UInt(n.W))
        val Sum = Output(UInt(n.W))
        val Cout = Output(UInt(1.W))
    })

    // FullAdder_practice ports: A B Cin Sum Cout
    // 宣告Module： Module(new {引入的Module name})
    val FA_Array = Array.fill(n)(Module(new FullAdder_practice()).io)
    val carry = Wire(Vec(n+1, UInt(1.W)))
    val sum = Wire(Vec(n, Bool()))

    // todo : 先設定第一個 Full_Adder
    carry(0) := io.Cin

    for (i <- 0 until n){
        FA_Array(i).A := io.In1(i)
        FA_Array(i).B := io.In2(i)
        FA_Array(i).Cin := carry(0)
        carry(i+1) := FA_Array(i).Cout
        sum(i) := FA_Array(i).Sum
    }

    io.Sum := sum.asUInt
    io.Cout := carry(n)
}