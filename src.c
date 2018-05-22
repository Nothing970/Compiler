int total;
float sum;
float max(float a,float b){
	float ret;
	ret	= a;
	if(a>=b){
		ret = a;
	}else{
		ret = b;
	}
	return ret;
}
int main(){
	char a[10];
	float b;
	float d;
	int i;
	i=0;
	printf("input array[10] of char:\n");
	while(i<10){
		scanf("%c",a[i]);
		getchar();
		i=i+1;
	}
	i=0;
	printf("before sort:\n");
	while(i<10){
		printf("a[%d] is %c\n", i,a[i]);
		i=i+1;
	}
	int times;
	times=1;
	while(times!=0){
		times=0;
		i=0;
		while(i<10-1){
			if(a[i]>a[i+1]){
				char tmp;
				tmp=a[i];
				a[i]=a[i+1];
				a[i+1]=tmp;
				times = times+1;
			}
			i=i+1;
		}
	}
	printf("after sort:\n");
	i=0;
	while(i<10){
		printf("a[%d] is %c\n", i,a[i]);
		i=i+1;
	}
	
	printf("input float b:");
	scanf("%f",b);
	printf("input float d:");
	scanf("%f",d);
	printf("b is %f\n",b);
	printf("d is %f\n",d);
	float maxnum;
	maxnum = max(b,d);
	printf("maxnum is %f\n",maxnum);
	
	printf("input int total:");
	scanf("%d",total);
	float j;
	j=0.0;
	sum=0.0;
	while(j<total){
		if(j/10.0==1){
			j=j+1;
			printf("continue\n");
			continue;
		}
		sum=sum+j;
		printf("sum is %f:\n",sum);
		j=j+1;
		if(sum>=1000){
			break;
		}
	}
	printf("sum is %f:\n",sum);
	
	return 0;
}
/* 	if(b>d){
		printf("b > d\n");
	}else{
		printf("b <= d\n");
	} */
	
/*  	printf("sum1 is %f\n",e/d);
	printf("sum2 is %f\n",2/d);
	printf("sum3 is %f\n",a/d);
	printf("sum1 is %f\n",e/2.5);
	printf("sum2 is %f\n",2/2.5);
	printf("sum3 is %f\n",a/2.5);
	printf("sum1 is %f\n",e/f);
	printf("sum2 is %f\n",2/f);
	printf("sum3 is %f\n",a/f);  */
	
/* 	printf("sum1 is %f\n",b/d);
	printf("sum2 is %f\n",9.1/d);
	printf("sum3 is %f\n",f/d);
	printf("sum1 is %f\n",b/11.3);
	printf("sum2 is %f\n",9.1/11.3);
	printf("sum3 is %f\n",f/11.3);
	printf("sum1 is %f\n",b/f);
	printf("sum2 is %f\n",9.1/f);
	printf("sum3 is %f\n",f/f);   */
	
/*  	printf("sum1 is %f\n",b/c);
	printf("sum2 is %f\n",9.1/c);
	printf("sum3 is %f\n",f/c);
	printf("sum1 is %f\n",b/11);
	printf("sum2 is %f\n",9.1/11);
	printf("sum3 is %f\n",f/11);
	printf("sum1 is %f\n",b/e);
	printf("sum2 is %f\n",9.1/e);
	printf("sum3 is %f\n",f/e);   */
	
/*  	printf("sum1 is %d\n",a/c);
	printf("sum2 is %d\n",a/e);
	printf("sum3 is %d\n",e/a);
	printf("sum4 is %d\n",a/1);
	printf("sum5 is %d\n",1/a);
	printf("sum6 is %d\n",1/e);
	printf("sum7 is %d\n",e/1);
	printf("sum8 is %d\n",2/1); 
	printf("sum8 is %d\n",e/1);  */
	
	/* 	char mm[10];
	char c;
	c = 'w'; 
	printf("sum1 is %c\n",c);
	scanf("%c",c);
	getchar();
	printf("sum1 is %c\n",c); 
	
	ch[4]='e';
	printf("sum1 is %c\n",ch[4]);
	scanf("%c",ch[4]);
	getchar();
	printf("sum1 is %c\n",ch[4]); 
	
	mm[6]='o';
	printf("sum1 is %c\n",mm[6]);
	scanf("%c",mm[6]);
	getchar();
	printf("sum1 is %c\n",mm[6]);  */
	
	/* 
	printf("sum1 is %d\n",a[3]);
	a[3]=e[2];
	printf("sum1 is %d\n",a[3]); */

/* int main (){
	int a , b = 1;
	int b = 1 , a;
	string c = "sj";
	char ch = 'i';
	float fa = 3.3;
	while(a==1){
		if(a>1){
			a=1;
		}
		break;
		if(a>1&&b!=1){
			a=1;
			continue;
		}else{
			a=1;
			break;
		}
		if(a!=1||a==b){
			a=1;
		}else if(a!=1||a==1&&!b<1){
			a=1;
		}else if(a==1){
			a=1;
		}
		if(a!=1||a==b){
			a=1;
		}else if(a!=1||a==1&&!b<1){
			a=1;
		}else if(a==1){
			a=1;
		}else{
			a=1;
		}	
		a=(1*3+4+(5)/7);
		a=a-1;
		while(!a<1){
			a=1;
			break;
		}
	}
} */

/* int a = b + 1;
while(a==0){
	if (a + b == 0 && b == 0){
		int c = a + b * 2;
	}
}
int b,a=3;
b=1;
if(a==1){
	b=3;
}
while((a>0||b>3)&&b==4){
	float c=0;
} */
