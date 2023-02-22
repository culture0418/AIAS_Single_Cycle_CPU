// in ./src/main/scala/aias_lab5/Hw4/PC

// PC：指令計數器，由於Memory是以Byte為存儲單位，但指令以Word為單位(4 Bytes)，所以以每週期以PC+4為主，唯有在遇到Branch、Jump系列指令才會遇到PC+offset。

package aias_lab5.Hw4

import chisel3._
import chisel3.util._

// 每次posedge都會+4的暫存器。

class PC extends Module{
    val io = IO(new Bundle{
        val brtaken = Input(Bool()) // choose branch
        val jmptaken = Input(Bool()) // choose jmp
        val offset = Input(UInt(32.W)) // todo : ALU output : target address
        val pc = Output(UInt(32.W))
    })

    val pcReg = RegInit(0.U(32.W))
    
    when (io.brtaken === 1.U){
        pcReg := Cat(io.offset(31,2), 0.U(2.W)) //無條件捨去offset的末兩位(改為0)
    }.elsewhen(io.jmptaken === 1.U){
        pcReg := Cat(io.offset(31,2), 0.U(2.W)) // 優點是沒有邏輯閘比較快 缺點是沒有邏輯閘
        // 寫法2 : (io.offset >> 2.U)<< 2.U
        // 寫法3 : (io.offset & "hfffffffc".U)
    }.otherwise{
        pcReg := pcReg + 4.U // no jump
    }
    
    // 比較好的寫法
    // Implementation #5
    // pcReg := Mux((io.brtaken || io.jmptaken), 
    //             Cat(io.offset(31,2), 0.U(2.W)), pcReg + 4.U) 

    io.pc := pcReg
}
