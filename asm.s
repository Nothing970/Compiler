.section .data

LC0: .ascii "sum is %d\n\0"
LC1: .ascii "sum is %d\n\0"
e: .int 0
f: .float 0
ch: .int 0, 0, 0, 0, 0, 0, 0, 0, 0, 0

.section .text


.globl _main
_main:
	pushl %ebp
	movl %esp, %ebp
	subl $24, %esp 
	movl $1, %eax
	movl %eax, -4(%ebp)
	movl $0, %eax
	movl %eax, -8(%ebp)
L0: movl -4(%ebp), %eax
	movl $10, %edx
	subl %edx, %eax
	movl %eax, -12(%ebp)
	movl -12(%ebp), %eax
	cmpl $0, %eax
	je L1
	jne L2
L1: movl -4(%ebp), %eax
	movl -8(%ebp), %edx
	addl %edx, %eax
	movl %eax, -16(%ebp)
	movl -16(%ebp), %eax
	movl %eax, -8(%ebp)
	movl $1, %eax
	movl -4(%ebp), %edx
	addl %edx, %eax
	movl %eax, -20(%ebp)
	movl -20(%ebp), %eax
	movl %eax, -4(%ebp)
	movl -8(%ebp), %eax
	pushl %eax
	pushl $LC0
	call _printf
	addl $8, %esp
	jmp L0
	jmp L0
L2: movl -8(%ebp), %eax
	pushl %eax
	pushl $LC1
	call _printf
	addl $8, %esp
	jmp L3
L3: movl $0, %eax
	leave
	ret
