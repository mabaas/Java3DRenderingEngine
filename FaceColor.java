/*
 *  @(#)FormColor.java	1.11 21-04-2005
 *  Colored 3D Models by Maron Baas
 *  maronbaas@hotmail.com
*/ 
import java.applet.Applet;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Event;
import java.awt.event.*;
import java.awt.*;
import java.math.*;
import java.util.*;
import java.io.*;
import java.net.URL;

class FileFormatException extends Exception 
{
   public FileFormatException(String s) 
   {   super(s);
   }
}

/** The representation of a 3D model */
class Model3D 
{
   float vert[];       // coordinaten
   int tvert[];        // vlakken met kleurcode
   int numberofvertices, maxvert;
   int PaneArray[];    // Array van vlakken met kleurcode
   int numberoflines = 0;
   int maxlines;
   boolean transformed;
   Matrix3D mat;
   float xmin, xmax, ymin, ymax, zmin, zmax;

   Model3D () 
   {     mat = new Matrix3D ();
	   mat.xrot(20);
	   mat.yrot(30);
   }

   /** Create a 3D model by parsing an input stream */
   Model3D (InputStream is) throws IOException, FileFormatException 
   {  this();
      StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(is)));
      st.eolIsSignificant(true);
      st.commentChar('#');
      scan:
	   while (true) 
      {
         switch (st.nextToken()) 
         {
	         default:
            break scan;
            case StreamTokenizer.TT_EOL:
            break;
            case StreamTokenizer.TT_WORD:
            if ("v".equals(st.sval)) 
		      {  double x = 0, y = 0, z = 0;
		         if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
		         {     x = st.nval;
			         if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
			         {  y = st.nval;
			            if (st.nextToken() == StreamTokenizer.TT_NUMBER)
				         z = st.nval;
			         }
		         }
		         addVert((float) x, (float) y, (float) z);
			      while (st.ttype != StreamTokenizer.TT_EOL &&
			      st.ttype != StreamTokenizer.TT_EOF)
			      st.nextToken();
            }
            else if ("f".equals(st.sval)) 
		      {  int vertex1 = 0; int vertex2 = 0;
                     int vertex3 = 0;int vertex4 = 0; int colorff = 0;
		         if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
                 {
                  vertex1 = (int) st.nval;
                  if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
                  {  vertex2 = (int) st.nval;
                     if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
                     {
                        vertex3 = (int) st.nval;
                        if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
                        {  vertex4 = (int) st.nval;
                           if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
                           {    colorff = (int) st.nval;
                           }
		            } 
                     }
	   	      }
	         addPane(vertex1 - 1, vertex2 - 1, vertex3 -1, vertex4 -1, colorff); 
	         while (st.ttype != StreamTokenizer.TT_EOL &&
	         st.ttype != StreamTokenizer.TT_EOF)
	         st.nextToken();
	         }
	      }
            else if ("t".equals(st.sval)) 
		{ int vertex1 = 0;  int vertex2 = 0;  int vertex3 = 0; int colorff = 0;
		   if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
               {
                  vertex1 = (int) st.nval;
                  if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
                  {  vertex2 = (int) st.nval;
                     if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
                     {
                        vertex3 = (int) st.nval;
                        if (st.nextToken() == StreamTokenizer.TT_NUMBER) 
                        { colorff = (int) st.nval;
			      } 
                     }
		   	}
			   addPane(vertex1 - 1, vertex2 - 1, vertex3 -1, colorff); 
			   while (st.ttype != StreamTokenizer.TT_EOL &&
				 st.ttype != StreamTokenizer.TT_EOF)
			     st.nextToken();
		      }
		    }
		 else 
		 {
		    while (st.nextToken() != StreamTokenizer.TT_EOL
			 && st.ttype != StreamTokenizer.TT_EOF);
		 }
         }
      }
      is.close();
      if (st.ttype != StreamTokenizer.TT_EOF)
         throw new FileFormatException(st.toString());
   }

   /** Add a vertex to this model */
   int addVert(float x, float y, float z) 
   {  int i = numberofvertices;
      if (i >= maxvert)
         if (vert == null) 
         {   maxvert = 100;
		 vert = new float[maxvert * 3];
	   } 
         else 
         {  maxvert *= 2;
            float nv[] = new float[maxvert * 3];
            System.arraycopy(vert, 0, nv, 0, vert.length);
            vert = nv;
         }
         i *= 3;
         vert[i] = x;
         vert[i + 1] = y;
         vert[i + 2] = z;
         return numberofvertices++;
   }
    
   /** Add a pane inclosing vertex1, vertex2, vertex3, vertex4 with color */
   /* Obsolete */
   void addPane(int p1, int p2, int p3, int p4, int color) 
   {
   	int i = numberoflines;
	if (p1 >= numberofvertices || p2 >= numberofvertices)
	   return;
      if (i >= maxlines)
      {
         if (PaneArray == null) 
         {  maxlines = 100;
		PaneArray = new int[maxlines*5];
         } 
         else 
         {
	      maxlines *= 3;
		int nv[] = new int[maxlines*5];
            // Make a copy
  		System.arraycopy(PaneArray, 0, nv, 0, PaneArray.length);
		PaneArray = nv;
         }
      }
	   if (p1 > p2) 
      {
	   int t = p1;
	   p1 = p2;
	   p2 = t;
	}
	i *= 5;
      PaneArray[i]   = p1;
      PaneArray[i+1] = p2;
      PaneArray[i+2] = p3;
      PaneArray[i+3] = p4;
      PaneArray[i+4] = color;	
	numberoflines++;
   }

   /** Add a pane inclosing vertex1, vertex2, vertex3 with color */
   void addPane(int p1, int p2, int p3, int color) 
   {
   	int i = numberoflines;
	   if (p1 >= numberofvertices || p2 >= numberofvertices)
	      return;
      if (i >= maxlines)
      {
         if (PaneArray == null) 
         {
	      maxlines = 100;
		PaneArray = new int[maxlines*4];
         } 
         else 
         {
		maxlines *= 3;
		int nv[] = new int[maxlines*4];
            // Make a copy
  		System.arraycopy(PaneArray, 0, nv, 0, PaneArray.length);
	      PaneArray = nv;
         }
      }
	if (p1 > p2) 
      {
	   int t = p1;
	   p1 = p2;
	   p2 = t;
	}
	i *= 4;
      PaneArray[i]   = p1;
      PaneArray[i+1] = p2;
      PaneArray[i+2] = p3;
      PaneArray[i+3] = color;	
	numberoflines++;
   }
    
      /** Add a pane inclosing vertex1, vertex2, vertex3, vertex4 with color */
   void addPane(int ArrayList[], int color) 
   {
      int i = ArrayList.length; 
   	//int i = numberoflines;
	if (i >= maxlines)
      {
         if (PaneArray == null) 
         {
		 maxlines = 100;
		 PaneArray = new int[maxlines*5];
         } 
         else 
         {
		maxlines *= 3;
	      int nv[] = new int[maxlines*5];
            // Make a copy
  		System.arraycopy(PaneArray, 0, nv, 0, PaneArray.length);
		PaneArray = nv;
         }
      }
	i *= 5;
      PaneArray[i]   = ArrayList[i];
      PaneArray[i+1] = ArrayList[i+1];
      PaneArray[i+2] = ArrayList[i+2];
      PaneArray[i+3] = ArrayList[i+3];
      PaneArray[i+4] = color;	
	   numberoflines++;
   }
	
   /** Transform all the points in this model */
   void transform() {
	if (transformed || numberofvertices <= 0)
	    return;
	if (tvert == null || tvert.length < numberofvertices * 3)
	    tvert = new int[numberofvertices*3];
	mat.transform(vert, tvert, numberofvertices);
	transformed = true;
    }

   /* Quick Sort implementation
    */
   private void quickSort(int a[], int left, int right)
   {
      int leftIndex = left;
      int rightIndex = right;
      int partionElement;
      if ( right > left)
      {
         partionElement = a[ ( left + right ) / 2 ];
         while( leftIndex <= rightIndex )
         {
            while(( leftIndex < right ) && ( a[leftIndex] < partionElement ))
               ++leftIndex;

            while(( rightIndex > left ) &&
                   ( a[rightIndex] > partionElement ))
               --rightIndex;
            // if the indexes have not crossed, swap
            if( leftIndex <= rightIndex )
            {  swap(a, leftIndex, rightIndex);
               ++leftIndex;
               --rightIndex;
            }
         }
         if( left < rightIndex )
            quickSort( a, left, rightIndex );
         if( leftIndex < right )
            quickSort( a, leftIndex, right );
      }
   }

   private void swap(int a[], int i, int j)
   {
      int T;
      T = a[i];
      a[i] = a[j];
      a[j] = T;
   }

   /** eliminate duplicate lines */
   void compress() 
   {
	int limit = numberoflines;
      int c[] = PaneArray;
	quickSort(PaneArray, 0, numberoflines - 1);
	int d = 0;
	int pp1 = -1;
	for (int i = 0; i < limit; i++) 
      {
	      int p1 = c[i];
	      if (pp1 != p1) 
         {
		      c[d] = p1;
		      d++;
	      }
	      pp1 = p1;
	   }
	   numberoflines = d;
   }

   /** Paint this model to a graphics context.  It uses the matrix associated
	with this model to map from model space to screen space.
	The next version of the browser should have double buffering,
	which will make this *much* nicer */
   void paint(Graphics gBuffer, Color myColors[]) 
   {
	   if (vert == null || numberofvertices <= 0)
	      return;
	   transform();
      int lim = numberoflines;
	   int c[] = PaneArray;
	   int v[] = tvert;
      int x[];
      int y[];
      x = new int [4];
      y = new int [4]; 
         
	   if (lim <= 0 || numberofvertices <= 0)
	      return;
	   for (int i = lim*5; (i-=5) >= 0; ) {
	      int p1 = c[i];
         int p2 = c[i+1];
         int p3 = c[i+2];
         int p4 = c[i+3];                 	    
         int col = c[i+4];  

	      gBuffer.setColor(myColors[col]);

         x[0] = v[p1*3  ];  y[0] = v[p1*3+1]; 
         x[1] = v[p2*3  ];  y[1] = v[p2*3+1]; 
         x[2] = v[p3*3  ];  y[2] = v[p3*3+1];
         x[3] = v[p4*3  ];  y[3] = v[p4*3+1];
         
         // coord[x,y,z] resulteert in vertCoord[x,y] (2dimensionaal) (z valt weg)  
         // Take the first and the last side of the polygon and calcute the outerproduct
         float v1 = x[1] - x[0];
         float w1 = x[3] - x[0];
         float v2 = y[1] - y[0];
         float w2 = y[3] - y[0]; 
         // If outerproduct is larger then 1 then the polygon is facing the screen
         if((v1*w2 - v2*w1) > 0)
         {
            gBuffer.fillPolygon(x,y,4);
         }
	    }
    }

   /** Find the bounding box of this model */
   void findBB() 
   {
	   if (numberofvertices <= 0)
	      return;
	   float v[] = vert;
	   float xmin = v[0], xmax = xmin;
	   float ymin = v[1], ymax = ymin;
      float zmin = v[2], zmax = zmin;
      for (int i = numberofvertices * 3; (i -= 3) > 0;) 
      {
	      float x = v[i];
	      if (x < xmin)
		      xmin = x;
	      if (x > xmax)
		      xmax = x;
	      float y = v[i + 1];
	      if (y < ymin)
		      ymin = y;
	      if (y > ymax)
		      ymax = y;
	      float z = v[i + 2];
	      if (z < zmin)
		      zmin = z;
	      if (z > zmax)
		     zmax = z;
	   }
	   this.xmax = xmax;
	   this.xmin = xmin;
	   this.ymax = ymax;
	   this.ymin = ymin;
	   this.zmax = zmax;
	   this.zmin = zmin;
   }
}

/** An applet to put a 3D model into a page */
public class FaceColor extends Applet
  implements Runnable, MouseListener, MouseMotionListener 
{
   Model3D md;
   Image Buffer;
   Graphics gBuffer;
   boolean painted = true;
   float xfac;
   int prevx, prevy;
   float xtheta, ytheta;
   float scalefudge = 1f;
   Matrix3D amat = new Matrix3D(), tmat = new Matrix3D();
   String mdname = null;
   String message = null;
   Color myColors[] = new Color[100];
   InitializeCubes cubes = new InitializeCubes();

   public void init() 
   {
      mdname = getParameter("models");
	   try {
	      scalefudge = Float.valueOf(getParameter("scale")).floatValue();
	   }catch(Exception e){};
	   amat.yrot(20);
	   amat.xrot(20);
	   if (mdname == null)
	      mdname = "3dcube.txt";
	   resize(getSize().width <= 20 ? 400 : getSize().width,
	   getSize().height <= 20 ? 400 : getSize().height);
	   addMouseListener(this);
	   addMouseMotionListener(this);
      cubes.FillColorArray(myColors);
      Buffer = createImage(getSize().width, getSize().height);
	   gBuffer = Buffer.getGraphics();
   }

   public void destroy() 
   {
      removeMouseListener(this);
      removeMouseMotionListener(this);
   }

   public void run() 
   {
	   InputStream is = null;
	   try {
	      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	      is = new URL(getDocumentBase(), mdname).openStream();
	      Model3D m = new Model3D (is);
	      md = m;
	      m.findBB();
		   float xw = m.xmax - m.xmin;
		   float yw = m.ymax - m.ymin;
		   float zw = m.zmax - m.zmin;
		   float hypotbb = (float)(Math.sqrt(xw*xw + yw*yw +zw*zw));
		   float f1 = getSize().width / hypotbb;
		   float f2 = getSize().height / hypotbb;
	      xfac = 0.7f * (f1 < f2 ? f1 : f2) * scalefudge;
	   } catch(Exception e) 
      {
	      md = null;
         message = e.toString();
	   }

	   try {
	      if (is != null)
		   is.close();
	   } catch(Exception e) {}
	   repaint();
   }

   public void start() 
   {  if (md == null && message == null)
	  new Thread(this).start();
   }
   public void stop()  { }
   public  void mouseClicked(MouseEvent e) {}
   public  void mousePressed(MouseEvent e) 
   {  prevx = e.getX();
      prevy = e.getY();
      e.consume();
   }
   public  void mouseReleased(MouseEvent e) {}
   public  void mouseEntered(MouseEvent e) {}
   public  void mouseExited(MouseEvent e) {}
   public  void mouseDragged(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();

      tmat.unit();
      float xtheta = -(prevy - y) * 360.0f / getSize().width;
      float ytheta = -(x - prevx) * 360.0f / getSize().height;
      tmat.xrot(xtheta);
      tmat.yrot(ytheta);
      amat.mult(tmat);
      if (painted) {
         painted = false;
         repaint();
      }
      prevx = x;
      prevy = y;
      e.consume();
   }
   public  void mouseMoved(MouseEvent e) {}   
   public void drawModel() 
   {
	   if (md != null) 
      {
	      md.mat.unit();
	      md.mat.translate(-(md.xmin + md.xmax) / 2,
			     -(md.ymin + md.ymax) / 2,
			     -(md.zmin + md.zmax) / 2);
	      md.mat.mult(amat);
	      md.mat.scale(xfac, -xfac, 16 * xfac / getSize().width);
	      md.mat.translate(getSize().width / 2, getSize().height / 2, 8);
         md.transformed = false;
         gBuffer.setColor(Color.white);
	      gBuffer.fillRect(0,0,getSize().width,getSize().height);
         // Paint my background buffer with the model.
         md.paint(gBuffer, myColors); 
	      setPainted();
	   } 
      else if (message != null) 
      {
         gBuffer.drawString("Error in model:", 3, 20);
         gBuffer.drawString(message, 10, 40);
	   }
   }
   public void update(Graphics g)
   {
	   if (Buffer == null)
	      g.clearRect(0, 0, getSize().width, getSize().height);
      paint(g);
   }
    
   public void paint(Graphics g){
    	drawModel();
    	g.drawImage(Buffer,0,0,this);
   } 
   private synchronized void setPainted() {
	   painted = true;
	   notifyAll();
   }

   public String getAppletInfo() {
      return "Title: ThreeD \nAuthor: Maron Baas? \nAn applet to put a 3D model into a page.";
   }
   public String[][] getParameterInfo() {
      String[][] info = {
            {"model", "path string", "The path to the model to be displayed."},
            {"scale", "float", "The scale of the model.  Default is 1."}
      };
      return info;
   }
}
