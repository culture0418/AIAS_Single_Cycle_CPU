# AIAS_Single_Cycle_CPU Chisel Tutorial && Datapath Design
修習人工智慧運算架構與系統所建立的 DataPath Design, testfile and test results 

##  Mix Adder
### Scala Code

```scala=
## scala code & comment
## make sure your have pass the MixAdderTest.scala

package aias_lab5.Hw1

import chisel3._
import chisel3.util._
import aias_lab5.Lab._

class MixAdder (n:Int) extends Module{
  val io = IO(new Bundle{
      val Cin = Input(UInt(1.W))
      val in1 = Input(UInt((4*n).W))
      val in2 = Input(UInt((4*n).W))
      val Sum = Output(UInt((4*n).W))
      val Cout = Output(UInt(1.W))
  })
  // wire each cladder
  // CLAdder ports: in1, in2, Cin, Sum, Cout
  val CLAdder_Array = Array.fill(8)(Module(new CLAdder()).io) // 32bit 所以有8個小單元cladder
  val carry = Wire(Vec(9, UInt(1.W)))
  val sum = Wire(Vec(8,UInt(4.W))) // each cladder unit output sum
  val four_bit_in1 = Wire(Vec(8, UInt(4.W)))
  val four_bit_in2 = Wire(Vec(8, UInt(4.W)))

  carry(0) := io.Cin
  
  // input 32 bit divided to 4_bit 8 group
  for (i <- 0 until 8){
    four_bit_in1(i) := Cat(io.in1(4*i+3), io.in1(4*i+2), io.in1(4*i+1), io.in1(4*i))
    four_bit_in2(i) := Cat(io.in2(4*i+3), io.in2(4*i+2), io.in2(4*i+1), io.in2(4*i))
  }
  
  // 4_bit_in1(0) = Cat(io.in1(0), io.in1(1), io.in1(2), io.in1(3)) 
  // 4_bit_in1(1) = Cat(io.in1(4), io.in1(5), io.in1(6), io.in1(7)) 
  // 4_bit_in1(2) = Cat(io.in1(8), io.in1(9), io.in1(10), io.in1(11))
  // 4_bit_in1(3) = Cat(io.in1(12), io.in1(13), io.in1(14), io.in1(15))

  for (i <- 0 until 8){
    CLAdder_Array(i).in1 := four_bit_in1(i) // each cladder input in1 按順序放式拆好的4_bit
    CLAdder_Array(i).in2 := four_bit_in2(i) // each cladder input in2 按順序放式拆好的4_bit
    CLAdder_Array(i).Cin := carry(i)
    carry(i+1) := CLAdder_Array(i).Cout
    sum(i) := CLAdder_Array(i).Sum
  }

  io.Sum := sum.asUInt
  io.Cout := CLAdder_Array(7).Cout
}
```
### Test Result
![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_62993b8e34219489c9d7245404452497.png)


## Add-Suber

### Scala Code
```scala=
## scala code & comment
## make sure your have pass the MixAdderTest.scala

// C:\Users\USER\Documents\aias\repo\playlab-docker-base\projects\Lab05\src\main\scala\aias_lab5\Hw2\Add_Suber.scala
package aias_lab5.Hw2

import chisel3._
import chisel3.util._
import aias_lab5.Lab._

class Add_Suber extends Module{
  val io = IO(new Bundle{
  val in_1 = Input(UInt(4.W))
	val in_2 = Input(UInt(4.W))
	val op = Input(Bool()) // 0:ADD 1:SUB
	val out = Output(UInt(4.W))
	val o_f = Output(Bool())
  })

  //please implement your code below

  // todo : op：operation，決定該做加法(0)還是減法(1)

  // Module Declaration
  // FullAdder : A B Cin Sum Cout
  val FA_Array = Array.fill(4)(Module(new FullAdder()).io)
  val NB = Wire(Vec(4,UInt(1.W))) // 放io.in_2 ^ io.op result
  val sum = Wire(Vec(4,Bool()))
  val carry = Wire(Vec(5,UInt(1.W)))
  
  carry(0) := io.op

  // Wiring
  for (i <- 0 until 4){
    NB(i) := io.in_2(i) ^ io.op // NB = io.in_2 XOR io.op
    FA_Array(i).A := io.in_1(i)
    FA_Array(i).B := NB(i)
    FA_Array(i).Cin := carry(i)
    carry(i+1) := FA_Array(i).Cout
    sum(i) := FA_Array(i).Sum
  }
  
  io.out := sum.asUInt
  // printf("sum = %x\n", sum.asUInt)
  io.o_f := carry(3)^carry(4)
}

```
### Test Result
![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_33891d97122224eded5eb01895edc6d1.png)

## Booth Multiplier
### Scala Code
#### Booth_Encoder
```scala=
## scala code & comment
## make sure your have pass the Booth_MulTest.scala

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

```
#### Booth_Mul
```scala
package aias_lab5.Hw3

import chisel3._
import aias_lab5._
import aias_lab5.Hw1._
import chisel3.util._
import scala.annotation.switch

//------------------Radix 4---------------------
class Booth_Mul(width:Int) extends Module {
  val io = IO(new Bundle{ 
    val in1 = Input(UInt(width.W))      //Multiplicand in1:A
    val in2 = Input(UInt(width.W))      //Multiplier in2:B
    val out = Output(UInt((2*width).W)) //product
  })
  //please implement your code below
  val Booth_Mul_Array = Array.fill(width/2)(Module(new Booth_Encoder(width)).io) // 每次看3bit，總共會有8組

  // Booth_encoder input: in1_e_A (in1:A), in2_e_B(in2:B), 
  //               output:Partial_Product

  val en_long_in2 = (Cat(io.in2, 0.U(1.W))).asUInt// tmp_in2 放加上 overlap 的 0 (16+1) bit
  val en_PP = Wire(Vec(width/2, SInt((width*2).W)))
  val sing_in1 = Wire(Vec(width/2, SInt((width*2).W)))

  for (i <- 0 until width/2){
    sing_in1(i) := io.in1.asSInt
    Booth_Mul_Array(i).in1_e_A := sing_in1(i).asUInt
    Booth_Mul_Array(i).in2_e_B := en_long_in2(2*(i+1), 2*i)
    en_PP(i) := (Booth_Mul_Array(i).Partial_Product) << (2*i)
    
  }
  // Booth_Mul_Array(0).in1_e_A := io.in1
  // Booth_Mul_Array(0).in2_e_B := en_long_in2(2,0)
  
  // Booth_Mul_Array(7).in1_e_A := io.in1
  // Booth_Mul_Array(7).in2_e_B := en_long_in2(16,14)

  // adder
  // 16 bit 3 adder_level
  // 32 bit 4 adder_level
  val adder_Sum_Array = Wire(Vec((width/2 - 1), SInt((2*width).W))) // 8 partial product 兩兩相加 需要7個adder
  var adder_level = (width)

  for(i <- 0 until (width/4)){
    adder_Sum_Array(i) := en_PP(2*i) + en_PP(2*i+1)
  }
  // first adder_level: 4 adders
  // adder_Sum_Array(0) := en_PP(0) + en_PP(1)
  // adder_Sum_Array(1) := en_PP(2) + en_PP(3)
  // adder_Sum_Array(2) := en_PP(4) + en_PP(5)
  // adder_Sum_Array(3) := en_PP(6) + en_PP(7)
  
  // second adder_level: 2 adders
  adder_Sum_Array(4) := adder_Sum_Array(0) + adder_Sum_Array(1)
  adder_Sum_Array(5) := adder_Sum_Array(2) + adder_Sum_Array(3)

  // third adder_level: 1 adder
  adder_Sum_Array(6) := adder_Sum_Array(4) + adder_Sum_Array(5)

  //operation
  io.out := adder_Sum_Array(6).asUInt
  
}

```
### Test Result
![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_efb95858dcb5c88e3cb44bde97648f66.png)

## Datapath Implementation for (3、4、7)
### PC
#### Scala Code

```scala=
## scala code & comment
## make sure your have pass the PCTest.scala
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

```
#### Test Result
![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_1534f166716057fc97631d3f48b84fa9.png)

### Decoder
#### Scala Code

```scala=
## scala code & comment
## make sure your have pass the DecoderTest.scala
package aias_lab5.Hw4

import chisel3._
import chisel3.util._

object opcode_map {
    val LOAD      = "b0000011".U    // Load opcode = 0000011
    val STORE     = "b0100011".U    // store opcode = 0100011
    val BRANCH    = "b1100011".U    // branch opcode = 1100011
    val JALR      = "b1100111".U    // jalr opcode = 1100111
    val JAL       = "b1101111".U    // jal opcode = 1101111
    val OP_IMM    = "b0010011".U    // I-Format opcode = 0010011
    val OP        = "b0110011".U    // op opcode = 0110011 (add,sub,...) R-format
    val AUIPC     = "b0010111".U    // AUIPC opcode = 0010111
    val LUI       = "b0110111".U    // lui opcode = 0110111
}

import opcode_map._

class Decoder extends Module{
    val io = IO(new Bundle{
        val inst = Input(UInt(32.W)) // instruction 32-bits

        //Please fill in the blanks by yourself
        val funct3 = Output(UInt(3.W)) // funct3 = inst[12-14]
        val funct7 = Output(UInt(7.W)) // funct7 = inst[25-31]
        val rs1 = Output(UInt(5.W)) // rs1 = inst[15-19]
        val rs2 = Output(UInt(5.W)) // rs2 = inst[20-24]
        val rd = Output(UInt(5.W)) // rd = inst[7-11]
        val opcode = Output(UInt(7.W)) // opcode = inst[0-6]
        val imm = Output(SInt(32.W)) // imm 由 immGen 擴充成 32 bit
        
        val ctrl_RegWEn = Output(Bool())  // for Reg write back
        val ctrl_ASel = Output(Bool()) // for alu src1 pc
        val ctrl_BSel = Output(Bool()) // for alu src2 imm
        val ctrl_Br = Output(Bool()) // for branch inst.
        val ctrl_Jmp = Output(Bool()) // for jump inst.
        val ctrl_Lui = Output(Bool()) // for lui inst.
        val ctrl_MemRW = Output(Bool()) // for L/S inst
        val ctrl_WBSel = Output(UInt(2.W)) // for write back select 3 type
    })

    //Please fill in the blanks by yourself
    io.funct3 := io.inst(14,12)
    io.funct7 := io.inst(31,25)
    io.rs1 := io.inst(19,15)
    io.rs2 := io.inst(24,20)
    io.rd := io.inst(11,7)
    io.opcode := io.inst(6,0)

    //ImmGen
    // core instruction formats
    io.imm := MuxLookup(io.opcode,0.S,Seq(
        //R-type no imm
        //Please fill in the blanks by yourself
        OP ->  0.S,

        //I-type
        OP_IMM -> io.inst(31,20).asSInt,
        
        JALR -> io.inst(31, 20).asSInt,
        
        LOAD -> io.inst(31, 20).asSInt,
        
        //B-type         imm[12],    imm[11],      imm[10,5],     imm[4,1],   imm[0]
        BRANCH -> (Cat(io.inst(31), io.inst(7), io.inst(30,25), io.inst(11,8), 0.U)).asSInt,

        //S-type
        STORE -> (Cat(io.inst(31,25), io.inst(11,7))).asSInt,

        //U-type
        AUIPC -> (io.inst(31,12) << 12).asSInt,

        LUI -> (io.inst(31,12) << 12).asSInt,
        
        //J-type
        JAL -> (Cat(io.inst(31), io.inst(19,12), io.inst(20), io.inst(30,21), 0.U)).asSInt,
        
    ))

    //Controller
    
    io.ctrl_RegWEn := MuxLookup(io.opcode, true.B, Seq(STORE -> false.B, BRANCH -> false.B)) // 不會寫回reg store, branch : false.B // other : true.B
    io.ctrl_ASel := MuxLookup(io.opcode, false.B, Seq(BRANCH -> true.B, JAL -> true.B, AUIPC -> true.B)) 
    // 有用到pc的 BRANCH、JAL、AUIPC、: true.B [jalr : pc+4(pc module會算好) or rs1 + imm 沒有用到pc]
    // other inst : false.B
    io.ctrl_BSel := Mux((io.opcode === OP),false.B, true.B) // r-type 沒有用到 imm :false.B , others : use imm : true.B 
    io.ctrl_Br := Mux((io.opcode === BRANCH), true.B, false.B) // branch : true.B // others: false.B
    io.ctrl_Jmp := MuxLookup(io.opcode, false.B, Seq(JALR -> true.B , JAL -> true.B)) // jump : jal、jalr: true.B // others : false.B
    io.ctrl_Lui :=  Mux((io.opcode === LUI), true.B, false.B) // lui : true.B 只看liu others : false.B
    io.ctrl_MemRW := Mux((io.opcode === STORE), true.B, false.B) // store : 去mem R or W : true.B // others : false.B
    io.ctrl_WBSel := MuxLookup(io.opcode, 1.U, Seq(LOAD -> 0.U, JALR -> 2.U, JAL -> 2.U))  // wb to reg
    // load: 0:dm data, others inst: 1:alu output, jalr, jal: 2:pc+4 
}
```
### Test Result
> 請放上你通過test的結果，驗證程式碼的正確性。(螢幕截圖即可)

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_c04edc1656b3b089df637c12a26a8bb6.png)

### BranchComp
#### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。

```scala=
## scala code & comment
## make sure your have pass the BranchCompTest.scala
package aias_lab5.Hw4

import chisel3._
import chisel3.util._

object condition{
  val EQ = "b000".U  //  beq funct3 000
  val NE = "b001".U  //  bne funct3 001
  val LT = "b100".U  //  blt funct3 100
  val GE = "b101".U  //  bge funct3 101
  val LTU = "b110".U  //  bltu funct3 110
  val GEU = "b111".U  //  bgeu funct3 111
}

import condition._

class BranchComp extends Module{
    val io = IO(new Bundle{
        val en = Input(Bool())
        val funct3 = Input(UInt(3.W))
        val src1 = Input(UInt(32.W))
        val src2 = Input(UInt(32.W))

        val brtaken = Output(Bool()) //for pc.io.brtaken
    })
    
    //please implement your code below
    
    // initialize
    io.brtaken := false.B

    when(io.en === true.B){
      io.brtaken := MuxLookup(io.funct3,false.B,
        Seq(
          EQ -> Mux((io.src1 === io.src2), true.B, false.B), // beq rs1, rs2, simm13
          NE -> Mux((io.src1 =/= io.src2), true.B, false.B), // bne rs1, rs2, simm13
          LT -> Mux((io.src1.asSInt < io.src2.asSInt), true.B, false.B), // blt rs1, rs2 (sign compare)
          GE -> Mux((io.src1.asSInt >= io.src2.asSInt), true.B, false.B), // bge rs1, rs2 (sign compare)
          LTU -> Mux((io.src1.asUInt < io.src2.asUInt), true.B, false.B), // bltu rs1, rs2 (unsign compare)
          GEU -> Mux((io.src1.asUInt >= io.src2.asUInt), true.B, false.B), // bgeu rs1, rs2 (unsign compare)
          ) 
        )
    } 
}
```
#### Test Result
> ![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_b62cb939d8744baa89c8fcfa67bcc2f5.png)


## Hw5-4 Datapath Implementation for (1、2、6、8、9)
### InstMem_wc.txt
> 含有(R、I、S、B、J type測資)
```=
//wc: with comment 寫給助教跟自己看的

//Hw5-4-1 I-type testing 
//inst[31,20]  inst[19,15]  inst[14,12]  inst[11,7]  inst[6,0]
// imm[11,0]      rs1          funct3        rd        opcode

//111111111111     00010          000        00001      0010011   // addi x1  x2 -1    , x1 = 1 ok
//000000000010     00100          001        00011      0010011   // slli x3  x4  2    , x3 = 4 * (2^2) = 16 ok
//000000000111     00110          010        00100      0010011   // slti x5  x6  7    , x5 = 1 ok
//000000001001     01000          011        00111      0010011   // sltui x7  x8  9   , x7 = 1 ok
//000000000001     00000          100        01001      0010011   // xori x9  x0  1 , x9 =  1 ok
//000000000001     00100          101        01001      0010011   // srli x12  x4  1 , x12 = 2 ok
//000000000001     00100          101        01001      0010011   // srai x13  x8  2 , x13 = 2 ok
//000000000001     00000          110        01010      0010011   // ori x10  x0  1 , x10 = 1 ok
//000000000010     00000          111        01011      0010011   // andi x11  x0 2 , x11 = 0 ok

//Hw5-4-2 R-type testing
// inst[31,25]  inst[24,20]  inst[19,15]  inst[14,12]  inst[11,7]   inst[6,0]
//   funct7         rs2          rs1        funct3         rd        opcode
//  0000000        01011        01001        000         10010      0110011     // add  x18  x9   x11 , x18 = 20 ok
//  0100000        00111        10101        000         10100      0110011     // sub  x20  x21  x7 , x20 = 14 ok
//  0000000        00001        01000        001         01110      0110011     // sll  x22  x8   x1 , x22 = 16 ok
//  0000000        11001        11000        010         10111      0110011     // slt  x23  x24  x25 , x23 = 1 ok
//  0000000        00001        01001        011         01010      0110011     // sltu x10  x9   x1  , x10 = 0 ok
//  0000000        00010        00001        100         01011      0110011     // xor  x11  x1   x2 , x11 = 3 ok
//  0000000        00001        00010        101         01011      0110011     // srl  x11  x2   x1 , x11 = 1 ok
//  0100000        00001        00010        101         01011      0110011     // sra  x11  x2   x1 , x11 = 1 ok
//  0000000        00111        00101        110         01011      0110011     // or   x11  x5   x7 , x11 = 7 ok
//  0000000        00010        00000        111         01011      0110011     // and  x11  x0   x2 , x11 = 0 ok

// Hw5-4-6 Load/Store inst. testing
//inst[31,20]  inst[19,15]  inst[14,12]  inst[11,7]  inst[6,0]
// imm[11,0]      rs1          funct3        rd        opcode

//000000000000     00000          000        00001      0000011   // lb x1 x0 0 
//000000000010     00000          001        00001      0000011   // lh x1 x0 2 
//000000000000     00000          010        00001      0000011   // lw x1 x0 0
//000000000011     00000          100        00001      0000011   // lbu x1 x0 3
//000000000010     00000          101        00001      0000011   // lhu x1 x0 0

// inst[31,25]  inst[24,20]  inst[19,15]  inst[14,12]  inst[11,7]   inst[6,0]
//  imm[11,5]       rs2          rs1        funct3      imm[4,0]     opcode 
//  0000000        00001        00000        000         00000      0100011   // sb x1 x0 0 
  0000000        00001        00000        001         00000      0100011   // sh x1 x0 0
//  0000000        00001        00000        010         00000      0100011   // sw x1 x0 0 

// Hw5-4-8 B-type testing
// inst[31]  inst[30,25]    inst[24,20]   inst[19,15]   inst[14,12]   inst[11,8]  inst[7]   inst[6,0]
// imm[12]    imm[10,5]         rs2          rs1          funct3       imm[4,1]   imm[11]    opcode
//   0         000000          00001        00001          000          0100        0        1100011   // beq x1 x1  8 ok
//   0         000000          00010        00001          001          1010        0        1100011   // bne x1 x2 20 ok
//   0         000000          00010        00001          100          1000        0        1100011   // blt x1 x2 16 ok
//   0         000000          00000        00001          101          0110        0        1100011   // bge x1 x0 12 ok
//   0         000000          00001        00000          110          0100        0        1100011   // bltu x0 x1 8 ok
//   0         000000          00001        00001          111          0110        0        1100011   // bgeu x1 x1 12 ok

// Hw5-4-9 J-type testing
// jal
// inst[31]  inst[30,21]    inst[20]   inst[19,12]   inst[11,7]     inst[6,0]
//  imm[20]   imm[10,1]      imm[11]    imm[19,12]       rd          opcode
//   0       0000000110         0        00000000       00001        1101111   // jal x1  12
    0       0000000110         0        00000000       00001        1101111     // jal x1  12
// jalr
// inst[31,20]   inst[19,15]   inst[14,12]   inst[11,7]    inst[6,0]
//  imm[11,0]      rs1           funct3         rd          opcode
000000010000    00100           000          00001       1100111   // jalr x1 x4 16
```

### Scala Code

- top
```scala=
## scala code & comment
## make sure you have comfirm the correctness of every type of instructions by yourself

package aias_lab5.Hw4

import chisel3._
import chisel3.util._

class top extends Module {
    val io = IO(new Bundle{
        val pc_out = Output(UInt(32.W))
        val alu_out = Output(UInt(32.W))
        val rf_wdata_out = Output(UInt(32.W))
        val brtaken_out = Output(Bool())
        val jmptaken_out = Output(Bool())
    })

    val pc = Module(new PC())
    val im = Module(new InstMem())
    val dc = Module(new Decoder())
    val rf = Module(new RegFile(2)) // rf(0) : rs1 // rf(1) : rs2
    val alu = Module(new ALU())
    val bc = Module(new BranchComp())
    val dm = Module(new DataMem())
    
    //PC
    pc.io.jmptaken := false.B // Don't modify
    pc.io.brtaken := false.B // Don't modify
    pc.io.offset := 0.U // Don't modify
    
    //Insruction Memory
    im.io.raddr := pc.io.pc
    
    //Decoder
    dc.io.inst := im.io.rdata
    
    //RegFile
    rf.io.raddr(0) := dc.io.rs1
    rf.io.raddr(1) := dc.io.rs2
    rf.io.wdata := MuxLookup(dc.io.ctrl_WBSel, 0.U,Seq(
        0.U -> dm.io.rdata.asUInt, // dmem rdata
        1.U -> alu.io.out,  // alu output
        2.U -> pc.io.pc, // pc + 4 FIXME:
    )) 
    
    rf.io.waddr := 0.U  // Don't modify
    rf.io.wen := false.B // Don't modify

    //ALU
    val rdata_or_zero = WireDefault(0.U(32.W))
    alu.io.src1 := Mux((dc.io.ctrl_ASel === 0.U), rf.io.rdata(0), pc.io.pc) // rf(0) : rs1
    alu.io.src2 := Mux((dc.io.ctrl_BSel === 1.U), dc.io.imm.asUInt, rf.io.rdata(1)) // rf(1) : rs2
    
    alu.io.funct3 := dc.io.funct3
    alu.io.funct7 := dc.io.funct7
    alu.io.opcode := dc.io.opcode
    
    //Data Memory input:　funct3 raddr wen waddr wdata , output: rdata 
    dm.io.funct3 := dc.io.funct3
    dm.io.raddr := alu.io.out
    dm.io.wen := dc.io.ctrl_MemRW
    dm.io.waddr := alu.io.out
    dm.io.wdata := rf.io.rdata(1)

    //Branch Comparator
    bc.io.en :=  dc.io.ctrl_Br 
    bc.io.funct3 := dc.io.funct3 // branch系列可根據funct3做區分
    bc.io.src1 := rf.io.rdata(0)
    bc.io.src2 := rf.io.rdata(1)

    //Check Ports
    io.pc_out := pc.io.pc
    io.alu_out := alu.io.out 
    io.rf_wdata_out := rf.io.wdata
    io.brtaken_out := bc.io.brtaken
    io.jmptaken_out := dc.io.ctrl_Jmp
}
```
### Test Result
> 請仿效文件上的截圖，放上各個type的指令驗證結果(每種type至少兩個，且與測資呼應。)
- R-type

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_3ce815ce39f985aea9ed4fbea234daab.png)

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_3f0308caadcfc56666f6cf5ae18334c7.png)

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_529265b462fd8604700c2bf5eb3040d4.png)

- I-type

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_851756d7ce4c0c6ba9058826e2d9a8d1.png)

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_b0a633dce2da83b65295edc60a59a29b.png)

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_4bb6df19cd07ec4b449ac9b523c272c3.png)

- load
    - lb x1 x0 0
    ![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_257a7ca6421a528a18a056188aafa897.png)
    - lh x1 x0 2
    ![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_14b722b5398ae7b60fcfe3361be9374c.png)
    - lw x1 x0 4
    ![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_1fef6dfa19acae26a1053e23e8e71b7b.png)
    - lbu x1 x0 3
    ![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_f51371e7a02b03f4e149b7d018b7740f.png)
    - lhu x1 x0 0
    ![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_de1562e10a969d3d7c4d9c3b997c92cd.png)
    
- S-type
    - sb x1 x0 0
    ![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_95ab8a480690ff05caaa68f6f326c37e.png)
    - sh x1 x0 0
    ![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_e6e9113a18779ccfc22bb84da0620597.png)

    - sw x1 x0 0
    ![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_e8d5133ecd0d3f3a35a886972a907bc8.png)


- B-type

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_e57a559634077f50b23b75c38e24ca8c.png)

![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_387de308704f318ef59d20c748d9d0c4.png)


![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_efe7467959bd28e145db1c3ecee607b7.png)

- J-type
![](https://playlab.computing.ncku.edu.tw:3001/uploads/upload_1a33ef3553441cb9fb09fd605b617a68.png)


## 文件中的問答題
- Q1:哪兩種type的指令需要仰賴ALU最多的功能呢？對於其他type的指令而言，ALU的功能是？
    - Ans1-1: I-type & R-type
    - Ans1-2: 
        - for branch, jal, jalr : pc + imm
- Q2:Branch情況發生時，**pc+offset**會從哪裡傳回pc呢？Beq x0 x0 imm 可以完全取代 jal x0 imm嗎？如果不行，為什麼?
    - Ans2-1 : alu.io.out
    - Ans2-2 : beq x0 x0 imm 雖然也會有 pc + imm
      但jal x0 imm 會做 R[rd]=pc+4，beq不會做pc+4
- Q3:假設我用int存取src1和src2，由於int是signed的方式存取，比大小(-1<3)自然能夠成立。有沒有什麼方式是**不需透過轉換Dtype**，就能夠實現Unsigned的比較方式呢?比如說，該如何驗證BGEU呢？(可以用pseudo code或者你認為你能表達清楚你的想法，用文字呈現也行。)
    - Ans3:
    - 

| Column 1 | io.src2    | io.src2     |
| -------- | ----------- | ---------- | 
| io.src1  | 正 正  s(1)  | 正 負 s(2) |     
| io.src1  | 負 正  s(3)  | 負 負 s(4) |     

```scala=
// bgeu rs1 rs2 imm
// s(1)
when(io.src1 > 0.S & io.src2 > 0.S){
    if(io.src1 >= io.src2){
        br.tk := 1.U
    }
}.elsewhen(io.src1 > 0 & io.src2 < 0){ // s(2)
    io.src2 = 0 - io.src2
    if(io.src1 >= io.src2){
        br.tk := 1.U
    }
}.elsewhen(io.src1 < 0 & io.src2 > 0){ // s(3)
    io.src1 = 0 - io.src1
    if(io.src1 >= io.src2){
        br.tk := 1.U
    }
}.otherwise{ //s(4)
    io.src1 = 0 - io.src1
    io.src2 = 0 - io.src2
    if(io.src1 >= io.src2){
        br.tk := 1.U
    }   
}S

```

