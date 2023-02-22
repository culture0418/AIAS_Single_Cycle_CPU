 // in ./src/main/scala/aias_lab5_Hw3/
package aias_lab5.Hw3

import chisel3._
import chisel3.util._ 
import scala.annotation.switch

//FIXME:先假設 width = 16 bits 
// encoder
class Booth_Encoder(width:Int) extends Module{
    val io = IO(new Bundle{
        val in1_e_A = Input(UInt((2*width).W)) // 16 bit // in1:Y:A
        val in2_e_B = Input(UInt(3.W)) // 16 bit // in2:X:B
        val Partial_Product = Output(SInt((2*width).W)) // 2*16 bits
})
    // initialize io.Partial_Product
    io.Partial_Product := 0.S
    val weight = Wire(SInt(4.W))
    weight := 0.S

    switch(io.in2_e_B){
        is("b000".U){ // 0
            weight := 0.S
            io.Partial_Product := 0.S // Partial_Product = 0 * io.in1_e_A = 0
        }
        is("b111".U){ // 0
            weight := 0.S
            io.Partial_Product := 0.S // Partial_Product = 0 * io.in1_e_A = 0
        }
        is("b001".U){ // 1
            weight := 1.S
            io.Partial_Product := (io.in1_e_A).asSInt // Partial_Product = 1 * io.in1_e_A 
        }
        is("b010".U){ // 1
            weight := 1.S
            io.Partial_Product := (io.in1_e_A).asSInt // Partial_Product = 1 * io.in1_e_A 
        }
        is("b011".U){ // 2
            weight := 2.S
            io.Partial_Product := ((io.in1_e_A)<<1).asSInt // Partial_Product = 2 * io.in1_e_A 
        }
        is("b100".U){ // -2
            weight := -2.S
            io.Partial_Product := ((~io.in1_e_A + 1.U(1.W))<<1).asSInt // Partial_Product = -2 * io.in1_e_A 
        }
        is("b101".U){ // -1
            weight := -1.S
            io.Partial_Product := (~io.in1_e_A + 1.U(1.W)).asSInt // Partial_Product = -1 * io.in1_e_A 
        }
        is("b110".U){ // -1
            weight := -1.S
            io.Partial_Product := (~io.in1_e_A + 1.U(1.W)).asSInt // Partial_Product = -1 * io.in1_e_A
        }
    }
     
    // println(io.Partial_Product)
    
}


