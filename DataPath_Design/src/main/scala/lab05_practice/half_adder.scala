// in  ./src/main/scala/lab05_practice/half_adder.scala

package lab05_practice

import chisel3._
import chisel3.util
import chisel3.stage.ChiselStage


class half_adder extends Module{
    // todo : declare IO port
    val io = IO(new Bundle{
        // input A B
        val A = Input(UInt(1.W))
        val B = Input(UInt(1.W))
        val Sum = Output(UInt(1.W))
        val Carry = Output(UInt(1.W)) 
        
    })
    // todo : 電路行為描述
    io.Sum := io.A ^ io.B
    io.Carry := io.A & io.B 
}

// todo : 準備入口函式
object half_adder extends App{
    (new chisel3.stage.ChiselStage).emitVerilog( //把chisel程式變成verilog程式
        new half_adder(), //FIXME:要間接執行的module(硬體元件)名稱
        Array("-td","generated/010")  //FIXME:產生的.v檔(verilog檔)的位置--->在路徑generated/<入口間接執行的module的name>裡
    )
}