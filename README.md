# A86_AssemblyInterpreter

@author Can Atakan Ugur https://github.com/bnbcfyh

@author Yusuf Bayindir  https://github.com/bayndrysf

@date   11.04.2020

Interpreter for an Assembly Language of a hypothetical 8086-like CPU called HYP86



I How to Simulate Memory
---------------------------------------------------------------------------------------------------
Memory is treated as a char array. An instruction area, over which any memory operation is prohibited,
is determined regarding the feauteres of HYP86. Char data type is not specified in Java as unsigned char data type in C++. It is handled through functions relevant to memory operations.


II About the Class Design
---------------------------------------------------------------------------------
There are 4 more classes beyond the Main class, through which register,label,data variable,and flag objects are created. The relation between the 16-bits registers that have 8-bits register parts(AX,BX,CX,DX) are constructed using OOP design features.
   

III How to Handle Syntax Errors
---------------------------------------------------------------------------------HYP86 checks the code using appropriate regular expressions, regarding exceptions fitting into these regular expressions. Any further checks, such as errors relevant to memory operations, instruction operations are performed when executing the code.



IV How to Read and Store Variables
---------------------------------------------------------------------------------
HYP86 reads,checks and stores variables before performing on code segment. In case that any 
issue are cought on data segment, HYP86 gives an error and terminates. Otherwise, store the data
variables through functions relevant to memory operations.



V How to Execute Instructions
---------------------------------------------------------------------------------------------------
All the code segment lines are performed respectively,  through functions relevant to current 
instruction. If any issue is the case, HYP86 gives an error and terminates. Otherwise, it does
all the necessary operations on memory,registers,flags.



VI MORE
--------------------------------------------------------------------------------
An integer class variable,named as instruction pointer, holds the current line number. In case that 
any jump instruction is the case, since the loop iteration integer is always equal to instruction pointer, HYP86 continues executing the code line that must be performed after the jump.

The relation between the high byte,lower byte, and 16 bit quantity general register is constructed within Register class.

 

