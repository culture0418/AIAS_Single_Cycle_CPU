// in ./src/main/scala/lab05_practice/CLAdder_practice.scala

package lab05_practice

// 引入需要的工具
import chisel3._ 
import chisel3.util._ 
import chisel3.stage.ChiselStage

// 撰寫 Module
// 要記得extend Module
class CLAdder_practice extends Module{
    // todo : 在Bundle內宣告 IO port
    // todo : 格式 : val 變數名稱 = Input/Output(dtype(長度))
    val io = IO(new Bundle{
        val in1 = Input(UInt(4.W))
        val in2 = Input(UInt(4.W))
        val Cin = Input(UInt(1.W))
        val S = Output(UInt(4.W))
        val Cout = Output(UInt(1.W))
    })

    // todo : Wire 的結構類似 C node

    val P = Wire(Vec(4,UInt()))
    val G = Wire(Vec(4,UInt()))
    val C = Wire(Vec(4,UInt()))
    val S = Wire(Vec(4,UInt()))

    for (i <- 0 until 4){
        G(i) := io.in1(i) & io.in2(i) // io.in1(i) * io.in2(i)
        P(i) := io.in1(1) | io.in2(i) // io.in1(i) + io.in2(i)
    }

    C(0) := io.Cin
    C(1) := G(0) |(P(0) & C(0))
    C(2) := G(1) |(P(1) & G(0))|(P(1) & P(0) & C(0))
    C(3) := G(2) |(P(2) & G(1))|(P(2) & P(1) & P(0) & C(0))


    // A B Cin Sum Cout
    val FA_Array = Array.fill(4)(Module(new FullAdder_practice).io) // 4 個加法器

    for (i <- 0 until 4){
        FA_Array(i).A := io.in1(i)
        FA_Array(i).B := io.in2(i)
        FA_Array(i).Cin := C(i)
        S(i) := FA_Array(i).Sum
    }

    io.S := S.asUInt
    io.Cout := FA_Array(3).Cout 


}
