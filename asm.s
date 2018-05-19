.section .data

LC0: .ascii "%d\n\0"
LC1: .ascii "%d\n\0"
.section .text

.globl _main
_main:
	pushl %ebp
	movl %esp, %ebp
	subl $12, %esp 
	movl $1, 1(%esp)
	movl $2, 5(%esp)
	movl 1(%esp), %eax
	pushl %eax
	pushl $LC0
	call _printf
	popl %eax
	popl %eax
	movl 1(%esp), %eax
	movl 5(%esp), %edx
	leal (%eax,%edx), %eax
	movl %eax, 9(%esp)
	movl 9(%esp), %eax
	pushl %eax
	pushl $LC1
	call _printf
	popl %eax
	popl %eax
	jmp L0
L0: 	movl $0, %eax
	leave
	ret
