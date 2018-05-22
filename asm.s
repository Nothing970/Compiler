.section .data

LC0: .ascii "input array[10] of char:\n\0"
LC1: .ascii "%c\0"
LC2: .ascii "before sort:\n\0"
LC3: .ascii "a[%d] is %c\n\0"
LC4: .ascii "after sort:\n\0"
LC5: .ascii "a[%d] is %c\n\0"
LC6: .ascii "input float b:\0"
LC7: .ascii "%f\0"
LC8: .ascii "input float d:\0"
LC9: .ascii "%f\0"
LC10: .ascii "b is %f\n\0"
LC11: .ascii "d is %f\n\0"
LC12: .ascii "maxnum is %f\n\0"
LC13: .ascii "input int total:\0"
LC14: .ascii "%d\0"
LC15: .float 0.0
LC16: .float 0.0
LC17: .float 10.0
LC18: .ascii "continue\n\0"
LC19: .ascii "sum is %f:\n\0"
LC20: .ascii "sum is %f:\n\0"
total: .int 0
sum: .float 0

.section .text


.globl _max
_max:
	pushl %ebp
	movl %esp, %ebp
	subl $12, %esp 
	movl 12(%ebp), %eax
	movl %eax, -4(%ebp)
	flds 12(%ebp)
	fcomp 8(%ebp)
	fnstsw %ax
	sahf
	jae L0
	jmp L1
L0: movl 12(%ebp), %eax
	movl %eax, -4(%ebp)
	jmp L2
	jmp L2
L1: movl 8(%ebp), %eax
	movl %eax, -4(%ebp)
	jmp L2
L2: jmp L3
L3: movl -4(%ebp), %eax
	leave
	ret

.globl _main
_main:
	pushl %ebp
	movl %esp, %ebp
	subl $192, %esp 
	movl $0, %eax
	movl %eax, -52(%ebp)
	pushl $LC0
	call _printf
	addl $4, %esp
L4: movl -52(%ebp), %eax
	movl $10, %edx
	cmpl %edx, %eax
	jl L5
	jmp L6
L5: movl -52(%ebp), %ebx
	movl -40(%ebp, %ebx, 4), %eax
	movl  %eax, -60(%ebp)
	movl $0, -40(%ebp, %ebx, 4)
	leal -40(%ebp, %ebx, 4), %eax
	pushl %eax
	pushl $LC1
	call _scanf
	addl $8, %esp
	call _getchar
	movl -52(%ebp), %eax
	movl $1, %edx
	addl %edx, %eax
	movl %eax, -64(%ebp)
	movl -64(%ebp), %eax
	movl %eax, -52(%ebp)
	jmp L4
	jmp L4
L6: movl $0, %eax
	movl %eax, -52(%ebp)
	pushl $LC2
	call _printf
	addl $4, %esp
L7: movl -52(%ebp), %eax
	movl $10, %edx
	cmpl %edx, %eax
	jl L8
	jmp L9
L8: movl -52(%ebp), %ebx
	movl -40(%ebp, %ebx, 4), %eax
	movl  %eax, -72(%ebp)
	movl -72(%ebp), %eax
	pushl %eax
	movl -52(%ebp), %eax
	pushl %eax
	pushl $LC3
	call _printf
	addl $12, %esp
	movl -52(%ebp), %eax
	movl $1, %edx
	addl %edx, %eax
	movl %eax, -76(%ebp)
	movl -76(%ebp), %eax
	movl %eax, -52(%ebp)
	jmp L7
	jmp L7
L9: movl $1, %eax
	movl %eax, -80(%ebp)
L10: movl -80(%ebp), %eax
	movl $0, %edx
	cmpl %edx, %eax
	jne L11
	jmp L17
L11: movl $0, %eax
	movl %eax, -80(%ebp)
	movl $0, %eax
	movl %eax, -52(%ebp)
L12: movl $10, %eax
	movl $1, %edx
	subl %edx, %eax
	movl %eax, -88(%ebp)
	movl -52(%ebp), %eax
	movl -88(%ebp), %edx
	cmpl %edx, %eax
	jl L13
	jmp L16
L13: movl -52(%ebp), %ebx
	movl -40(%ebp, %ebx, 4), %eax
	movl  %eax, -96(%ebp)
	movl -52(%ebp), %eax
	movl $1, %edx
	addl %edx, %eax
	movl %eax, -100(%ebp)
	movl -100(%ebp), %ebx
	movl -40(%ebp, %ebx, 4), %eax
	movl  %eax, -104(%ebp)
	movl -96(%ebp), %eax
	movl -104(%ebp), %edx
	cmpl %edx, %eax
	jg L14
	jmp L15
L14: movl -52(%ebp), %ebx
	movl -40(%ebp, %ebx, 4), %eax
	movl  %eax, -116(%ebp)
	movl -116(%ebp), %eax
	movl %eax, -112(%ebp)
	movl -52(%ebp), %eax
	movl $1, %edx
	addl %edx, %eax
	movl %eax, -120(%ebp)
	movl -120(%ebp), %ebx
	movl -40(%ebp, %ebx, 4), %eax
	movl  %eax, -124(%ebp)
	movl -124(%ebp), %eax
	movl -52(%ebp), %ebx
	movl %eax, -40(%ebp, %ebx, 4)
	movl -52(%ebp), %eax
	movl $1, %edx
	addl %edx, %eax
	movl %eax, -128(%ebp)
	movl -112(%ebp), %eax
	movl -128(%ebp), %ebx
	movl %eax, -40(%ebp, %ebx, 4)
	movl -80(%ebp), %eax
	movl $1, %edx
	addl %edx, %eax
	movl %eax, -132(%ebp)
	movl -132(%ebp), %eax
	movl %eax, -80(%ebp)
	jmp L15
L15: movl -52(%ebp), %eax
	movl $1, %edx
	addl %edx, %eax
	movl %eax, -136(%ebp)
	movl -136(%ebp), %eax
	movl %eax, -52(%ebp)
	jmp L12
	jmp L12
L16: jmp L10
	jmp L10
L17: pushl $LC4
	call _printf
	addl $4, %esp
	movl $0, %eax
	movl %eax, -52(%ebp)
L18: movl -52(%ebp), %eax
	movl $10, %edx
	cmpl %edx, %eax
	jl L19
	jmp L20
L19: movl -52(%ebp), %ebx
	movl -40(%ebp, %ebx, 4), %eax
	movl  %eax, -144(%ebp)
	movl -144(%ebp), %eax
	pushl %eax
	movl -52(%ebp), %eax
	pushl %eax
	pushl $LC5
	call _printf
	addl $12, %esp
	movl -52(%ebp), %eax
	movl $1, %edx
	addl %edx, %eax
	movl %eax, -148(%ebp)
	movl -148(%ebp), %eax
	movl %eax, -52(%ebp)
	jmp L18
	jmp L18
L20: pushl $LC6
	call _printf
	addl $4, %esp
	leal -44(%ebp), %eax
	pushl %eax
	pushl $LC7
	call _scanf
	addl $8, %esp
	pushl $LC8
	call _printf
	addl $4, %esp
	leal -48(%ebp), %eax
	pushl %eax
	pushl $LC9
	call _scanf
	addl $8, %esp
	flds -44(%ebp)
	subl $8, %esp
	fstpl 0(%esp)
	pushl $LC10
	call _printf
	addl $12, %esp
	flds -48(%ebp)
	subl $8, %esp
	fstpl 0(%esp)
	pushl $LC11
	call _printf
	addl $12, %esp
	pushl -48(%ebp)
	pushl -44(%ebp)
	call _max
	addl $8, %esp
	movl %eax, -156(%ebp)
	movl -156(%ebp), %eax
	movl %eax, -152(%ebp)
	flds -152(%ebp)
	subl $8, %esp
	fstpl 0(%esp)
	pushl $LC12
	call _printf
	addl $12, %esp
	pushl $LC13
	call _printf
	addl $4, %esp
	movl $total, %eax
	pushl %eax
	pushl $LC14
	call _scanf
	addl $8, %esp
	movl LC15, %eax
	movl %eax, -160(%ebp)
	movl LC16, %eax
	movl %eax, sum
L21: movl total, %eax
	movl %eax, -164(%ebp)
	fildl -164(%ebp)
	fcomp -160(%ebp)
	fnstsw %ax
	sahf
	ja L22
	jmp L27
L22: flds -160(%ebp)
	fdivs LC17
	fstps -168(%ebp)
	movl $1, -172(%ebp)
	fildl -172(%ebp)
	fcomp -168(%ebp)
	fnstsw %ax
	sahf
	je L23
	jmp L24
L23: movl $1, -176(%ebp)
	fildl -176(%ebp)
	fadds -160(%ebp)
	fstps -176(%ebp)
	movl -176(%ebp), %eax
	movl %eax, -160(%ebp)
	pushl $LC18
	call _printf
	addl $4, %esp
	jmp L21
L24: flds -160(%ebp)
	fadds sum
	fstps -180(%ebp)
	movl -180(%ebp), %eax
	movl %eax, sum
	flds sum
	subl $8, %esp
	fstpl 0(%esp)
	pushl $LC19
	call _printf
	addl $12, %esp
	movl $1, -184(%ebp)
	fildl -184(%ebp)
	fadds -160(%ebp)
	fstps -184(%ebp)
	movl -184(%ebp), %eax
	movl %eax, -160(%ebp)
	movl $1000, -188(%ebp)
	fildl -188(%ebp)
	fcomp sum
	fnstsw %ax
	sahf
	jbe L25
	jmp L26
L25: jmp L27
L26: jmp L21
	jmp L21
L27: flds sum
	subl $8, %esp
	fstpl 0(%esp)
	pushl $LC20
	call _printf
	addl $12, %esp
	jmp L28
L28: movl $0, %eax
	leave
	ret
