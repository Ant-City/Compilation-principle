package bit.minisys.minicc.codegen;

import bit.minisys.minicc.symbol.SymbolEntryFunc;
import bit.minisys.minicc.symbol.SymbolTable;
import bit.minisys.minicc.tree.TreeNode;
import bit.minisys.minicc.tree.TreeNodeType;

public class genifCode {
	private String iftrue="iftrue";
	private String iffalse="iffalse";
	public String GenifCode(TreeNode root, SymbolTable st, SymbolEntryFunc sef,MIPS mips,String times)
	{
		String code="";
		this.iftrue+=times;
		this.iffalse+=times;
		code+=genifCode(root, st,sef,mips);
		return code;
	}
	public String genifCode(TreeNode root, SymbolTable st, SymbolEntryFunc sef,MIPS mips){
	String code = "";
	TreeNodeType type = root.getType();
	boolean genNext=false;
	if(type == TreeNodeType.TNT_ICONST)
	{
		code += "\tli " +mips.getRegName(root.getRegNum()) + ", ";
		String value = "" + root.getName();
		code += value + "\n";
		
	}//��ֵ���  a=scanf
	else if(type == TreeNodeType.TNT_ASNMT_EXP)
	{
		TreeNode left = root.getByIndex(0);
		TreeNode right = root.getByIndex(2);
		//evaluation right
		if(root.getByIndex(2).getType() == TreeNodeType.TNT_CONST)
		{
			code+="\tli "+"$t0"+ ","+right.getName()+"\n";
			code+="\tmove " + mips.getRegName(left.getRegNum()) + ","+"$t0\n";
		}
		else
		{
			code += genifCode(root.getByIndex(2), st, sef, mips);
			//generate 
		
			if(left.getRegNum() != right.getRegNum()){
				code += "\tmove "+mips.getRegName(left.getRegNum()) + ", " +mips.getRegName(right.getRegNum()) + "\n";
			}
		}
		
		
	}//ADD a+b
	else if(type == TreeNodeType.TNT_ADD_EXP)
	{
		TreeNode op1 = root.getByIndex(0);
		TreeNode op2 = root.getByIndex(2);
		
		String opc = "";
		String reg1 = "";
		String reg2 = "";
		String reg3 = "";
		
		//�������
		code += genifCode(op1, st, sef, mips);
		code += genifCode(op2, st, sef, mips);
		
		reg1=mips.getRegName(root.getRegNum());
		if(op1.getType() == TreeNodeType.TNT_CONST && op2.getType() == TreeNodeType.TNT_CONST)
		{
			code += "\taddi " + reg1 + ", $zero, " + op1.getName() + "\n"; 
			reg2 = reg1;
			
		}
		else if(op1.getType() == TreeNodeType.TNT_CONST)
		{
			reg2 =mips.getRegName(op2.getRegNum());
			reg3 = op1.getName();
		}
		else if(op2.getType() == TreeNodeType.TNT_CONST)
		{
			reg2 =mips.getRegName(op1.getRegNum());
			reg3 = op2.getName();
		}
		else
		{
			reg2 =mips.getRegName(op1.getRegNum());
			reg3 =mips.getRegName(op2.getRegNum());
		}
		
		if(op1.getType() == TreeNodeType.TNT_CONST || op2.getType() == TreeNodeType.TNT_CONST)
		{
			if(root.getByIndex(1).getName().equals("+")){
				code += "\taddi ";
			}else if(root.getByIndex(1).getName().equals("-")){
				code += "\tsubi ";
			} 
		}
		else
		{
			if(root.getByIndex(1).getName().equals("+")){
				code += "\tadd ";
			}else if(root.getByIndex(1).getName().equals("-")){
				code += "\tsub ";
			}
		}

		if(type == TreeNodeType.TNT_ADD_EXP){
			code += reg1 + ", " + reg2 + ", " + reg3 + "\n";
		}
	}
	//JMP
	else if(type == TreeNodeType.TNT_JMP_STMT)
	{
		TreeNode expr = root.getByIndex(1);
		code += genifCode(expr, st, sef, mips);
		if(expr.getChildren().size() == 1 && expr.getByIndex(0).getType() == TreeNodeType.TNT_CONST){
			code += "\taddi $v0, $v0, " + expr.getByIndex(0).getName() + "\n";
		}else{
			code += "\tmove $v0, " +mips.getRegName(expr.getRegNum()) + "\n";
		}
	}   //i++   i--
	else if(type==TreeNodeType.TNT_POST_EXP)
	{
		if(root.getByIndex(1).getName().equals("++")){
			code+="\taddi "+mips.getRegName(root.getByIndex(0).getRegNum())+","
			+mips.getRegName(root.getByIndex(0).getRegNum())+",1"+"\n";
		}
		if(root.getByIndex(1).getName().equals("--")){
			code+="\taddi "+mips.getRegName(root.getByIndex(0).getRegNum())+","
			+mips.getRegName(root.getByIndex(0).getRegNum())+",-1"+"\n";
		}
		
	}  
	/*�ж�a>0   a<0  ���ںţ�"&gt;" С�ںţ�"&lt;" ���ںţ�==
	  ���ڵ��ڣ�"&gt;="   С�ڵ��ڣ�"&lt;="
	  beq	$t0,$t1,target	#  branch to target if  $t0 = $t1
	blt	$t0,$t1,target	#  branch to target if  $t0 < $t1
	ble	$t0,$t1,target	#  branch to target if  $t0 <= $t1
	bgt	$t0,$t1,target	#  branch to target if  $t0 > $t1
	bge	$t0,$t1,target	#  branch to target if  $t0 >= $t1*/
	else if(type==TreeNodeType.TNT_REL_EXP)
	{
		TreeNode op1 = root.getByIndex(0);
		TreeNode op2 = root.getByIndex(2);
		String number1="";
		String number2="";
		if((op1.getType() == TreeNodeType.TNT_CONST)&&(op2.getType() == TreeNodeType.TNT_CONST))
		{
			number1=op1.getName();
			number2=op2.getName();
		}
		else if(op1.getType() == TreeNodeType.TNT_CONST)
		{
			number1=op1.getName();
			number2=mips.getRegName(op2.getRegNum());
		}
		else if(op2.getType() == TreeNodeType.TNT_CONST)
		{
			number1=mips.getRegName(op1.getRegNum());
			number2=op2.getName();
		}
		else
		{
			number1=mips.getRegName(op1.getRegNum());
			number2=mips.getRegName(op2.getRegNum());
		}
		if(root.getByIndex(1).getName().equals(">"))//����
		{
			//bgt	$t0,$t1,target	#  branch to target if  $t0 > $t1
			//ble	$t0,$t1,target	#  branch to target if  $t0 < $t1С����������ѭ�����֧��䣬���������
			//bgt	$t0,$t1,target	#  branch to target if  $t0 > $t1
			code+="\tbgt "+number1+","+number2+","+iftrue+"\n";
			code+="\tble "+number1+","+number2+","+iffalse+"\n";
			code+="\n"+iftrue+":\n";
		}
		if(root.getByIndex(1).getName().equals("<"))//С��
		{
			//bgt	$t0,$t1,target	#  branch to target if  $t0 > $t1
			//blt	$t0,$t1,target	#  branch to target if  $t0 < $t1���ڵ���������ѭ�����֧��䣬С�������
			code+="\tblt "+number1+","+number2+","+iftrue+"\n";
			code+="\tbge "+number1+","+number2+","+iffalse+"\n";//���ڵ���
			code+="\n"+iftrue+":\n";
		}
		if(root.getByIndex(1).getName().equals("<="))//С�ڵ���
		{
			//ble	$t0,$t1,target	#  branch to target if  $t0 <= $t1
			//bgt	$t0,$t1,target	#  branch to target if  $t0 > $t1 ��������ѭ�����֧��䣬С�ڵ��������
			code+="\tble "+number1+","+number2+","+iftrue+"\n";
			code+="\tbgt "+number1+","+number2+","+iffalse+"\n";//���ڵ���
			code+="\n"+iftrue+":\n";
		}
		if(root.getByIndex(1).getName().equals(">="))//���ڵ���
		{
			//bge	$t0,$t1,target	#  branch to target if  $t0 >= $t1С������ѭ�����֧��䣬���ڵ��������
			//blt	$t0,$t1,target	#  branch to target if  $t0 < $t1
			code+="\tbge "+number1+","+number2+","+iftrue+"\n";
			code+="\tblt "+number1+","+number2+","+iffalse+"\n";
			code+="\n"+iftrue+":\n";
		}
	}
	else
	{
		genNext = true;
	}
	
	if(genNext)
	{
		for(TreeNode c: root.getChildren()){
			code += genifCode(c, st, sef, mips);
		}
	}
	if(root.getType() == TreeNodeType.TNT_SEL_STMT_IF){
		code+="\n"+iffalse+":\n";
	}
	
	return code;
}
}
