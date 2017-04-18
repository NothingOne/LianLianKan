import java.awt.*;
import java.applet.*;
import java.util.*;
import javax.swing.JOptionPane;

public class llk extends Applet {
	
	//////////////可根据游戏需求更改////////////////
	//int mapWidth = 10;
	//int mapHeight = 8;
	int mapWidth = 14;	//游戏总共的行列数目，乘积必须为偶数
	int mapHeight = 10;
	//int width =72,height=72;
	int width =45,height=45;	//图片宽度和高度
	//int KindsOfPic = 9;
	int KindsOfPic = 22;	//图片种类
	
	//////////////不可更改////////////////
	int PicWidth = mapWidth - 2;	//图片坐标范围
	int PicHeight = mapHeight - 2;
	int SumPic = PicWidth * PicHeight;	//剩余图片总数
	int IndexOfKong;		//图片"空"的索引
	//////////////////////////////
	
	Image[] img = new Image[ KindsOfPic + 8 ];
	int[][] map = new int[mapHeight][mapWidth];	//0:null
	
	Image kong;
	Stack stack = new Stack();	//路径栈
	
	public void init() {
		this.setBackground(Color.black);	//可以修改游戏的背景色
		
		//pic
		for( int i = 1; i <= KindsOfPic; ++i ) {
			String filename = Integer.toString(i) + ".jpg";
			img[i] = getImage( this.getCodeBase(),filename );
		}
		
		//FangXiang(方向图片)
		img[KindsOfPic + 1] = getImage(this.getCodeBase(),"zy.jpg");
		img[KindsOfPic + 2] = getImage(this.getCodeBase(),"sx.jpg");
		img[KindsOfPic + 3] = getImage(this.getCodeBase(),"zs.jpg");
		img[KindsOfPic + 4] = getImage(this.getCodeBase(),"zx.jpg");
		img[KindsOfPic + 5] = getImage(this.getCodeBase(),"ys.jpg");
		img[KindsOfPic + 6] = getImage(this.getCodeBase(),"yx.jpg");

		//Selected
		IndexOfKong = KindsOfPic + 7;
		img[IndexOfKong] = getImage(this.getCodeBase(),"kong.jpg");
	}
	
	public void mapInit(int ranP, Vector vec) {	//在MAP中随机找到空元素
	
		if( vec.size()==0 )
			JOptionPane.showMessageDialog(null,"mapInit failed!!");
		int ranMapElement = (int)(Math.random()*vec.size());
		int i = ((MyPair)vec.get(ranMapElement)).first();
		int j = ((MyPair)vec.get(ranMapElement)).second();
		map[i][j]=ranP;
		vec.removeElementAt(ranMapElement);
	}

	public void start() {	//初始化MAP为成对的随机图片
		for( int i = 0; i < mapHeight; ++i)
			for( int j = 0; j < mapWidth; ++j )
				map[i][j] = 0;

		Vector vecMapElement = new Vector();
		for( int i = 1; i <= PicHeight; ++i )
			for( int j = 1; j <= PicWidth; ++j )
				vecMapElement.addElement(new MyPair(i,j,0));

		while( vecMapElement.size() > 0 ) {
			int ranP = (int)(Math.random()*KindsOfPic)+1;
			mapInit(ranP,vecMapElement);
			mapInit(ranP,vecMapElement);
		}
		while(!CheckMap())
			LuanXu();
		repaint();
	}
	
	public void paint(Graphics g) {		//绘制屏幕
		for( int i = 0; i < mapHeight; ++i)
			for( int j = 0; j < mapWidth; ++j)
				if(map[i][j] != 0)
					g.drawImage( img[map[i][j]],j*width,i*height,this);
	}
	
////////////////////////////////////////////////////////////////////////
////////////////////////////Press Picture///////////////////////////////
////////////////////////////////////////////////////////////////////////
	boolean choosed = false;//已有图片被选中
	boolean show = false;//处于显示路径状态
	int starI = 0,starJ = 0,starO = 0;//保存选中的第一个图片的信息
	int Itmp,Jtmp;//当前被选中图片的信息
	public boolean mouseDown(Event e,int x,int y)
	{
		if(show) {
			SumPic-=2;
			show =false;
			map[starI][starJ]=0;
			map[Itmp][Jtmp] = 0;
			ClearOther();
			if(SumPic <= 0) {
				JOptionPane.showMessageDialog(null,"Congratulations");
				SumPic=PicWidth * PicHeight;
				start();
				return true;
			}
			while(!CheckMap())
				LuanXu();				
			return true;
		}
		Itmp = y/height;
		Jtmp = x/width;
		if(Itmp>0 && Itmp<=PicHeight && Jtmp>0 && Jtmp<=PicWidth && map[Itmp][Jtmp]>0 && map[Itmp][Jtmp]<=KindsOfPic)
			if(!choosed) {
				starI = Itmp;
				starJ = Jtmp;
				starO = map[starI][starJ];
				map[starI][starJ] = IndexOfKong;
				choosed = true;
				repaint();
			}
			else
				if( map[Itmp][Jtmp] != starO)
					ScrResume();
				else
					if( LookForPath(starI,starJ,Itmp,Jtmp) ) {
						map[starI][starJ]=starO;
						show = true;
						ShowPicInStack();
						choosed = false;
					}
					else
						ScrResume();
		else
			if(choosed)
				ScrResume();
		return true;
	}
	
	/////////////////////////////////////////////////////////////////////////
	
	public void ScrResume() {
		map[starI][starJ] = starO;
		choosed = false;
		repaint();
	}
	
	public void ShowPicInStack() {	//显示路径上的图片
		Node tmp;
		while(stack.size() > 1) {
			tmp = (Node)stack.pop();
			if(tmp.getPicIndex()==0)
				JOptionPane.showMessageDialog(null,"ShowPicInStack failed");
			map[tmp._x][tmp._y] = tmp.getPicIndex() + KindsOfPic;
		}
		repaint();
	}
	
	public void ClearOther() {
		for( int i = 0; i < mapHeight; ++i )
			for( int j= 0; j < mapWidth; ++j )
				if( map[i][j] < 0 || map[i][j] > KindsOfPic )
					map[i][j] = 0;
		repaint();
	}
	
	public boolean CheckMap()	//消除一对图片后,进行检查
	{
		Vector vecMap = new Vector();
		for(int i = 1; i <= PicHeight; ++i)
			for(int j = 1; j <= PicWidth; ++j)
				if(map[i][j]!=0)
					vecMap.addElement( new MyPair(i,j,map[i][j]) );

		while( vecMap.size() > 1)
		{
			
			for(int i = 1; i < vecMap.size(); i++)
				if( ((MyPair)vecMap.elementAt(0)).thrid() == 
					((MyPair)vecMap.elementAt(i)).thrid() )
						
					if(	LookForPath( ((MyPair)vecMap.elementAt(0)).first(),
							((MyPair)vecMap.elementAt(0)).second(),
							((MyPair)vecMap.elementAt(i)).first(),
							((MyPair)vecMap.elementAt(i)).second()
						)
					)
						return true;
			vecMap.removeElementAt(0);
		}
		return false;
	}
	public void LuanXu()	//重洗现有图片
	{
		for(int i = 0; i < 200; ++i) {
			int ranI = (int)(Math.random()*PicHeight)+1;
			int ranJ = (int)(Math.random()*PicWidth)+1;
			int tmp;
			tmp = map[1][1];
			map[1][1]=map[ranI][ranJ];
			map[ranI][ranJ] = tmp;
		}
	} 
	
	
	///////////////////////////////////////////////////////////////////////////
	/////////////////////////////Look for Path///////////////////////////////
	////////////////////////////寻找路径的算法////////////////////////////////
	public boolean LookForPath(int i1,int j1,int i2,int j2)
	{
		int[][] move = {
			{-1,1,0,0},
			{0,0,-1,1}
		};//4个方向上的坐标偏移量
		
		//stack.removeAllElements();
		Vector vecStack = new Vector();
		CloneableStack stkPath = new CloneableStack();
		Node hnd = new Node(i1,j1);
		stkPath.push(hnd);

		while(true) {
			
			int turnoff = 0;	//turnoff count
			
			for( int i =0; i < stkPath.size(); ++i )	//计算路径中拐角个数
				if( ((Node)stkPath.elementAt(i)).TurnOff() )
					turnoff++;
					
			Node tmp = (Node)stkPath.peek();	//tmp为当前栈中顶节点
			if(tmp.closed())
				if(stkPath.size() == 1)	//如果头节点关闭,则两点间无路径,退出寻路函数
					if( vecStack.isEmpty() )
						return false;
					else {
						//find min stack
						stack = (Stack)( vecStack.firstElement() );
						for( int i = 0; i < vecStack.size(); ++i )
							if( ( (Stack)vecStack.get(i) ).size() < stack.size() )
								stack = (Stack)vecStack.get(i);
						return true;
					}
				else {
					stkPath.pop();
					( (Node)stkPath.peek() ).NextOutWay();
				}
			else
				if( turnoff <3 ) {
					int nextI ,nextJ;	//ii,jj为根据栈顶元素出口方向 算出的下个节点坐标
					nextI=tmp.I()+move[0][tmp.CurOutWay() - 1];
					nextJ=tmp.J()+move[1][tmp.CurOutWay() - 1];
					
					if(nextI>=0 && nextI<mapHeight && nextJ>=0 && nextJ<mapWidth)
					//如果下个节点在图片区范围内...
						if(nextI==i2 && nextJ==j2) {
							//find a path Copy stack to vector
							vecStack.addElement( (Stack)stkPath.clone() );
							tmp.NextOutWay();
						}
						else
							if( map[nextI][nextJ]==0 )
								//如果路点可通行,则路点入栈
								stkPath.push( new Node(nextI,nextJ,tmp.TurnOver()) );
							else
								tmp.NextOutWay();
					else
						tmp.NextOutWay();
				}
				else
					tmp.NextOutWay();
				//如果节点当前方向的试探失败了,则转到下个方向试探
		}
	}
}

class Node {
	int _x,_y;//node ZuoBiao;
	int in,out;// 1:north;2:south;3:west;4:east;
	//current in/out way//该节点的入口方向和出口方向
	boolean head;
	
	public Node(int x,int y,int in)	//节点的构造函数
	{
		_x = x;
		_y = y;
		this.in = in;
		if((out = in +1) > 4 )
			out = 1;
		head = false;
	}
	public Node(int x,int y) {	//头节点的构造函数
		_x=x;_y=y;out=1;in=5;head=true;
	}

	public boolean closed()
	//Is node no way?//节点各方向的路径探测都失败,即为关闭的节点
	{
		if(out == in)
			return true;
		else
			return false;
	}

	public boolean NextOutWay() {	//改变节点的出口方向为下一方向
	//true: have way
	//false: closed
	// Warinng!!:different of "closed" method
		if( out == in )
			return false;
		if(++out == in)
			return false;
		if(out > 4)
			if((out = 1) == in)
				return false;
		return true;
	}

	public boolean TurnOff()
	//turn off?//该节点是否是转弯
	{
		if(head)
			return false;
		if((in == 1 || out == 1) && (in == 2 || out ==2))
				return false;
		if((in == 3 && out == 4) || (in == 4 && out ==3))
				return false;
		return true;
	}
	public int getPicIndex() {	//节点对应的方向图片号
		if((in==1 && out==2) || (in==2 && out==1))
			return 2;//sx
		if((in==3 && out==4) || (in==4 && out==3))
			return 1;//zy
		if((in==1 && out==3) || (in==3 && out==1))
			return 3;//zs
		if((in==1 && out==4) || (in==4 && out==1))
			return 5;//ys
		if((in==2 && out==3) || (in==3 && out==2))
			return 4;//zx
		if((in==2 && out==4) || (in==4 && out==2))
			return 6;//yx
		return 0;//failed
	}
	public int I() {
		return _x;
	}
	public int J() {
		return _y;
	}
	public int CurOutWay()
	{
		if(out == in)
			return 0;
		return out;
	}
	public int TurnOver()	//节点出口方向的反向,用来给出下个节点的入口方向
	{
		if(out == 1 || out == 3)
			return out+1;
		if(out == 2 || out == 4)
			return out-1;
		return 0;
	}
	
	public Object clone()
	{
		Node node = new Node(_x,_y);
		node.in = in;
		node.out = out;
		node.head = head;
		return node;
	}
}

class MyPair{	//每个图片的坐标和其图片号码的合体类型
	int i,j,k;
	public MyPair(int x, int y, int z) {
		i=x;j=y;k=z;
	}
	public int first() {
		return i;
	}
	public int second() {
		return j;
	}
	public int thrid() {
		return k;
	}
}

class CloneableStack extends java.util.Stack implements Cloneable {	//copy Stack
	public Object clone() {
		Stack stack = new Stack();
		for (int i = 0; i < this.size(); i++)
			stack.push( ((Node)get(i)).clone() );
		return stack;
	}
}