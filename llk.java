import java.awt.*;
import java.applet.*;
import java.util.*;
import javax.swing.JOptionPane;

public class llk extends Applet {
	
	//////////////�ɸ�����Ϸ�������////////////////
	//int mapWidth = 10;
	//int mapHeight = 8;
	int mapWidth = 14;	//��Ϸ�ܹ���������Ŀ���˻�����Ϊż��
	int mapHeight = 10;
	//int width =72,height=72;
	int width =45,height=45;	//ͼƬ��Ⱥ͸߶�
	//int KindsOfPic = 9;
	int KindsOfPic = 22;	//ͼƬ����
	
	//////////////���ɸ���////////////////
	int PicWidth = mapWidth - 2;	//ͼƬ���귶Χ
	int PicHeight = mapHeight - 2;
	int SumPic = PicWidth * PicHeight;	//ʣ��ͼƬ����
	int IndexOfKong;		//ͼƬ"��"������
	//////////////////////////////
	
	Image[] img = new Image[ KindsOfPic + 8 ];
	int[][] map = new int[mapHeight][mapWidth];	//0:null
	
	Image kong;
	Stack stack = new Stack();	//·��ջ
	
	public void init() {
		this.setBackground(Color.black);	//�����޸���Ϸ�ı���ɫ
		
		//pic
		for( int i = 1; i <= KindsOfPic; ++i ) {
			String filename = Integer.toString(i) + ".jpg";
			img[i] = getImage( this.getCodeBase(),filename );
		}
		
		//FangXiang(����ͼƬ)
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
	
	public void mapInit(int ranP, Vector vec) {	//��MAP������ҵ���Ԫ��
	
		if( vec.size()==0 )
			JOptionPane.showMessageDialog(null,"mapInit failed!!");
		int ranMapElement = (int)(Math.random()*vec.size());
		int i = ((MyPair)vec.get(ranMapElement)).first();
		int j = ((MyPair)vec.get(ranMapElement)).second();
		map[i][j]=ranP;
		vec.removeElementAt(ranMapElement);
	}

	public void start() {	//��ʼ��MAPΪ�ɶԵ����ͼƬ
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
	
	public void paint(Graphics g) {		//������Ļ
		for( int i = 0; i < mapHeight; ++i)
			for( int j = 0; j < mapWidth; ++j)
				if(map[i][j] != 0)
					g.drawImage( img[map[i][j]],j*width,i*height,this);
	}
	
////////////////////////////////////////////////////////////////////////
////////////////////////////Press Picture///////////////////////////////
////////////////////////////////////////////////////////////////////////
	boolean choosed = false;//����ͼƬ��ѡ��
	boolean show = false;//������ʾ·��״̬
	int starI = 0,starJ = 0,starO = 0;//����ѡ�еĵ�һ��ͼƬ����Ϣ
	int Itmp,Jtmp;//��ǰ��ѡ��ͼƬ����Ϣ
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
	
	public void ShowPicInStack() {	//��ʾ·���ϵ�ͼƬ
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
	
	public boolean CheckMap()	//����һ��ͼƬ��,���м��
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
	public void LuanXu()	//��ϴ����ͼƬ
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
	////////////////////////////Ѱ��·�����㷨////////////////////////////////
	public boolean LookForPath(int i1,int j1,int i2,int j2)
	{
		int[][] move = {
			{-1,1,0,0},
			{0,0,-1,1}
		};//4�������ϵ�����ƫ����
		
		//stack.removeAllElements();
		Vector vecStack = new Vector();
		CloneableStack stkPath = new CloneableStack();
		Node hnd = new Node(i1,j1);
		stkPath.push(hnd);

		while(true) {
			
			int turnoff = 0;	//turnoff count
			
			for( int i =0; i < stkPath.size(); ++i )	//����·���йսǸ���
				if( ((Node)stkPath.elementAt(i)).TurnOff() )
					turnoff++;
					
			Node tmp = (Node)stkPath.peek();	//tmpΪ��ǰջ�ж��ڵ�
			if(tmp.closed())
				if(stkPath.size() == 1)	//���ͷ�ڵ�ر�,���������·��,�˳�Ѱ·����
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
					int nextI ,nextJ;	//ii,jjΪ����ջ��Ԫ�س��ڷ��� ������¸��ڵ�����
					nextI=tmp.I()+move[0][tmp.CurOutWay() - 1];
					nextJ=tmp.J()+move[1][tmp.CurOutWay() - 1];
					
					if(nextI>=0 && nextI<mapHeight && nextJ>=0 && nextJ<mapWidth)
					//����¸��ڵ���ͼƬ����Χ��...
						if(nextI==i2 && nextJ==j2) {
							//find a path Copy stack to vector
							vecStack.addElement( (Stack)stkPath.clone() );
							tmp.NextOutWay();
						}
						else
							if( map[nextI][nextJ]==0 )
								//���·���ͨ��,��·����ջ
								stkPath.push( new Node(nextI,nextJ,tmp.TurnOver()) );
							else
								tmp.NextOutWay();
					else
						tmp.NextOutWay();
				}
				else
					tmp.NextOutWay();
				//����ڵ㵱ǰ�������̽ʧ����,��ת���¸�������̽
		}
	}
}

class Node {
	int _x,_y;//node ZuoBiao;
	int in,out;// 1:north;2:south;3:west;4:east;
	//current in/out way//�ýڵ����ڷ���ͳ��ڷ���
	boolean head;
	
	public Node(int x,int y,int in)	//�ڵ�Ĺ��캯��
	{
		_x = x;
		_y = y;
		this.in = in;
		if((out = in +1) > 4 )
			out = 1;
		head = false;
	}
	public Node(int x,int y) {	//ͷ�ڵ�Ĺ��캯��
		_x=x;_y=y;out=1;in=5;head=true;
	}

	public boolean closed()
	//Is node no way?//�ڵ�������·��̽�ⶼʧ��,��Ϊ�رյĽڵ�
	{
		if(out == in)
			return true;
		else
			return false;
	}

	public boolean NextOutWay() {	//�ı�ڵ�ĳ��ڷ���Ϊ��һ����
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
	//turn off?//�ýڵ��Ƿ���ת��
	{
		if(head)
			return false;
		if((in == 1 || out == 1) && (in == 2 || out ==2))
				return false;
		if((in == 3 && out == 4) || (in == 4 && out ==3))
				return false;
		return true;
	}
	public int getPicIndex() {	//�ڵ��Ӧ�ķ���ͼƬ��
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
	public int TurnOver()	//�ڵ���ڷ���ķ���,���������¸��ڵ����ڷ���
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

class MyPair{	//ÿ��ͼƬ���������ͼƬ����ĺ�������
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