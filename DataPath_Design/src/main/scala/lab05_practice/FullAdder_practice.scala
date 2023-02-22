// TODO: in ./lab05/src/main/scala/lab05_practice/FullAdder_practice.scala

package lab05_practice

import chisel3._ 
import chisel3.util._ 
import chisel3.stage.ChiselStage

// todo : Module

class FullAdder_practice extends Module{
    val io = IO(new Bundle{
        // todo : 在Bundle內宣告 IO port
        // todo : 格式 : val 變數名稱Input/Output(dtype(長度))
        val A = Input(UInt(1.W))
        val B = Input(UInt(1.W))
        val Cin = Input(UInt(1.W))
        val Sum = Output(UInt(1.W))
        val Cout = Output(UInt(1.W))
        
    })

    //FIXME:欲使用的module如果在同一個package，直接宣告即可
    //FIXME:如果在不同package，就需要引入該module所在的package

    // todo : Module Declaration
    val ha1 = Module(new half_adder())
    val ha2 = Module(new half_adder())

    // todo : wiring
    ha1.io.A := io.A
    ha1.io.B := io.B

    ha2.io.A := ha1.io.Sum
    ha2.io.B := io.Cin

    io.Sum := ha2.io.Sum
    io.Cout := ha1.io.Carry | ha2.io.Carry
}


// todo : 準備入口函式
object FullAdder_practice extends App{
    (new chisel3.stage.ChiselStage).emitVerilog( //把chisel程式變成verilog程式
        new FullAdder_practice(), //FIXME:要間接執行的module(硬體元件)名稱
        Array("-td","generated/000")  //FIXME:產生的.v檔(verilog檔)的位置--->在路徑generated/<入口間接執行的module的name>裡
    )
}