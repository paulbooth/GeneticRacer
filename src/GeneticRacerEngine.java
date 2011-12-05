import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class GeneticRacerEngine extends JFrame{
	Random RNG=new Random();
	int gameWidth=800, gameHeight=600,
	shooterX=gameWidth-50,
	numOfRacers=500;
	ArrayList<Racer> racers=new ArrayList<Racer>(numOfRacers);
	ArrayList<Bullet> bullets=new ArrayList<Bullet>(0);
	boolean racing=false, speedUp=false;
	GenePool genePool=new GenePool();
	Shooter shooter=new Shooter();
	
	public GeneticRacerEngine(){
		super("Genetic Racer!");
		toFront(); setVisible(true);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setCursor(
				getToolkit().createCustomCursor(
						getToolkit().createImage(""),
						new Point(0,0),
						""
				)
		);
		setLocation(
				(int)(1280-gameWidth)/2,
				(int)(800-gameHeight)/2-27
		);

		setPreferredSize(new Dimension(gameWidth,gameHeight));
		final GeneticRacerPanel GRP=new GeneticRacerPanel();
		getContentPane().add(GRP);
		//setSize(Engine.getScreenSize());

		setFocusable(false);
		setResizable(false);
		pack(); show();
		update();
		Thread run=new Thread(){
			int MAX_FRAMESKIP=10, TICKS_PER_SECOND=50,SKIP_TICKS=1000 / TICKS_PER_SECOND;

			public void run(){
				long next_game_tick = System.currentTimeMillis();
				int loops;
				while(true){
					loops = 0;
					while( System.currentTimeMillis() > next_game_tick 
							&& loops < MAX_FRAMESKIP) {
						if (speedUp)
							for(int i=0;i<5;i++)
								update();
						update();
						next_game_tick += SKIP_TICKS;
						loops++;
					}

					GRP.repaint();
				}
			}

		};
		run.start();
	}
	public void update(){
		shooter.update();
		if (!racing){
			//bullets.clear();
			if (racers.size()>0){
				if (numOfRacers>2)
				numOfRacers-=1;
				
			}else
				numOfRacers+=3;
			racers.clear();
			for(int i=0;i<numOfRacers;i++){
				racers.add(genePool.createRacer());
			}
			genePool.updatePool();
			
			racing=true;
		}
		else{
			for(int i=0;i<racers.size();i++){
				if (!racers.get(i).update())
				{
					if (racers.get(i).getPosx()+racers.get(i).getWidth()/2>gameWidth){
						racing=false;
						genePool.addRacer(racers.get(i));
					}
					else{
						racers.remove(i);
						i--;
					}
				}
			}
			for(int i=0;i<bullets.size();i++){
				bullets.get(i).update();
				for(int j=0;j<racers.size();j++){
					if (racers.get(j).getRect().intersects(bullets.get(i).getRect()))
					{
						
						if (!racers.get(j).healthHurt(
								bullets.get(i).getWidth()*bullets.get(i).getHeight()
							//	*(Math.abs(bullets.get(i).color.getRed()-racers.get(j).getColor().getRed())+
								//Math.abs(bullets.get(i).color.getGreen()-racers.get(j).getColor().getGreen())+
								//Math.abs(bullets.get(i).color.getBlue()-racers.get(j).getColor().getBlue())
								//)/370*bullets.get(i).color.getGreen()/125
						)){
							shooter.bulletColor=racers.get(j).getColor();
							racers.remove(j);
						}
						bullets.remove(i);
						i--;
						break;
					}
				}
			}
			if (racers.size()<1) racing=false;
		}

	}
	public void draw(Graphics g){
		shooter.draw(g);
		for(int i=0;i<bullets.size();i++)
			if (bullets.get(i)!=null)
			bullets.get(i).draw(g);
		for(int i=0;i<racers.size();i++){
			if (racers.get(i)!=null)
				racers.get(i).draw(g);
		}
	}
	private class Racer{
		int height=RNG.nextInt(50), width=RNG.nextInt(50);
		double speedx=RNG.nextDouble()*3+.1,speedy=RNG.nextDouble()*1-.5;
		Color color=new Color(RNG.nextInt(255),RNG.nextInt(255),RNG.nextInt(255));
		double posx=0, posy=RNG.nextInt(gameHeight), originalPosy=posy;
		int hatchTime=RNG.nextInt(2000)+20, hatchTimer=0;
		int health=width*height, maxHealth=health;
		int longevity=5;
		public Racer(){}
		public Racer(int height, int width, double speedx, double speedy,
				Color color, double posx, double posy, int hatchtime, int maxHealth) {
			super();
			this.height = height;
			this.width = width;
			this.speedx = speedx;
			this.speedy = speedy;
			this.color = color;
			this.posx = posx;
			this.posy = posy;
			this.originalPosy=posy;
			this.hatchTime = hatchtime;
			this.health=height*width;
			this.maxHealth=this.health;
		}
		public boolean healthHurt(int damage){
			health-=damage*(255)/125;
			return health>0;
		}
		
		public int getHealth() {
			return health;
		}
		public void setHealth(int health) {
			this.health = health;
		}
		public int getMaxHealth() {
			return maxHealth;
		}
		public void setMaxHealth(int maxHealth) {
			this.maxHealth = maxHealth;
		}
		public int getHeight() {
			return height;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public double getSpeedx() {
			return speedx;
		}
		public void setSpeedx(double speedx) {
			this.speedx = speedx;
		}
		public double getSpeedy() {
			return speedy;
		}
		public void setSpeedy(double speedy) {
			this.speedy = speedy;
		}
		public Color getColor() {
			return color;
		}
		public void setColor(Color color) {
			this.color = color;
		}
		public double getPosx() {
			return posx;
		}
		public void setPosx(double posx) {
			this.posx = posx;
		}
		public double getPosy() {
			return posy;
		}
		public double getOriginalPosy(){
			return originalPosy;
		}
		public void setPosy(double posy) {
			this.posy = posy;
		}
		public int getHatchTime() {
			return hatchTime;
		}
		public void setHatchTime(int hatchtime) {
			this.hatchTime = hatchtime;
		}
		public Rectangle2D getRect(){
			return new Rectangle2D.Double(posx-width/2, posy-height/2,width,height);
		}
		public boolean update(){
			if (hatchTimer<hatchTime){
				hatchTimer++;
				return true;
			}else{
			posx+=speedx;
			posy+=speedy;//6JQ32-Y9CGY-3Y986-HDQKT-BPFPG
			return posx+width/2<gameWidth
			&&posx>-width/2
			&&posy>-height/2+50
			&&posy<gameHeight+height/2-50;
			}
		}
		public void draw(Graphics g){
			if (hatchTimer<hatchTime){
				g.setColor(Color.white);
				int widthTemp=width*hatchTimer/hatchTime, heightTemp=height*hatchTimer/hatchTime;
				g.fillRect((int)(posx-width/2), (int)(posy-height/2), width, height);
				g.setColor(Color.black);
				g.drawRect((int)(posx-widthTemp/2), (int)(posy-heightTemp/2),
						widthTemp, heightTemp);
			}
			else{
			g.setColor(color);
			g.fillRect((int)(posx-width/2), (int)(posy-height/2), width, height);
			g.setColor(Color.black);
			g.drawRect((int)(posx-width/2), (int)(posy-height/2), width, height);
			}
			//System.out.println("px:"+posx+"\tpy:"+posy+"\tsx:"+speedx+"\tsy:"+speedy);
		}

	}
	private class GenePool{
		ArrayList<Racer> winners=new ArrayList<Racer>(0);
		public void addRacer(Racer r){
			winners.add(r);
		}
		public Racer createRacer(){
			Racer r=selectRacerFromPool();
			r=addRandomMutations(r);
			return r;
		}
		public Racer addRandomMutations(Racer r){
			r=new Racer(
					(int)(r.getHeight()*RNG.nextFloat()*2+3),
					(int)(r.getWidth()*RNG.nextFloat()*2+3),
					r.getSpeedx()*(RNG.nextFloat()*.5+.75),
					r.getSpeedy()*(RNG.nextFloat()*.5+.75),

					new Color(
							Math.min(Math.max(r.getColor().getRed()+RNG.nextInt(50)-25,0),255),
							Math.min(Math.max(r.getColor().getGreen()+RNG.nextInt(50)-25,0),255),
							Math.min(Math.max(r.getColor().getBlue()+RNG.nextInt(50)-25,0),255)),

					0,
					Math.min(Math.max(r.getOriginalPosy()+RNG.nextInt(100)-50,0),gameHeight),
					(int)(r.getHatchTime()*(RNG.nextFloat()*.5*+.75)),
					(int)(r.getMaxHealth()*(RNG.nextFloat()*.65+.7))

			);
			return r;
		}
		public Racer selectRacerFromPool(){
			Racer r=new Racer();
			if (!winners.isEmpty()){
				int num=0;
				for (int i=0;i<RNG.nextInt(winners.size());i++){
					int j=RNG.nextInt(winners.size());
					Racer chosen=(j<winners.size()?winners.get(j):new Racer());
					num++;
					r=meld(r,num,chosen);
				}
				
			}
			meld(r,5,new Racer());
			return r;
		}
		public Racer meld(Racer r,int num, Racer chosen){
			return new Racer(
					((r.getHeight()*num)+chosen.getHeight())/(num+1),
					((r.getWidth()*num)+chosen.getWidth())/(num+1),
					((r.getSpeedx()*num)+chosen.getSpeedx())/(num+1),
					((r.getSpeedy()*num)+chosen.getSpeedy())/(num+1),

					new Color(
							((r.getColor().getRed()*num)+chosen.getColor().getRed())/(num+1),
							((r.getColor().getGreen()*num)+chosen.getColor().getGreen())/(num+1),
							((r.getColor().getBlue()*num)+chosen.getColor().getBlue())/(num+1)),

					0,
					((r.getOriginalPosy()*num)+chosen.getOriginalPosy())/(num+1),
					((r.getHatchTime()*num)+chosen.getHatchTime())/(num+1),
					((r.getMaxHealth()*num)+chosen.getMaxHealth())/(num+1)

			);
		}
		public void updatePool(){
			for (int i=0;i<winners.size();i++){
				winners.get(i).longevity--;
				if (winners.get(i).longevity<0&&RNG.nextBoolean()){
					winners.remove(i);
					i--;
				}
			}
		}
	}
	private class GeneticRacerPanel extends JPanel{
		public GeneticRacerPanel(){
			setFocusable(true);
			setBackground(Color.cyan);
			setSize(gameWidth,gameHeight);
			//setPreferredSize(getSize());

			Robot macro;
			try {
				macro = new Robot();
				macro.mouseMove(gameWidth/ 2, 
						gameWidth / 2);
			} catch (Exception e) {
				e.printStackTrace();
			}
			addMouseListener(new MouseAdapter(){
				@Override
				public void mousePressed(MouseEvent me) {
					
					shooter.posy=me.getY();
					shooter.shoot();
				}
			});
			addMouseMotionListener(new MouseMotionAdapter(){
				@Override
				public void mouseMoved(MouseEvent me){
					shooter.posy=me.getY();
				}
				@Override
				public void mouseDragged(MouseEvent me){
					shooter.posy=me.getY();
					shooter.shoot();
				}
			});
			requestFocus();

		}
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			draw(g);
		}
	}
	private class Shooter{
		int width=50, height=100;
		Color bulletColor=new Color(255,0,0);
		double posy=gameHeight/2;
		int timer=0, maxTime=5;
		boolean canShoot=true;
		public void update(){
			timer++;
			if (timer>maxTime){
				timer=0;
				canShoot=true;
			}
		}
		public void shoot(){
			if (canShoot){
				bullets.add(new Bullet(shooterX,posy));
				canShoot=false;
				timer=0;
			}
		}
		public void draw(Graphics g){
			g.setColor(new Color(200,100,100));
			g.fillRect(shooterX, (int)posy-height/2, width, height);
			g.setColor(Color.black);
			g.drawRect(shooterX, (int)posy-height/2, width, height);
			g.fillRect(shooterX, (int)posy-height/8, width/2, height/4);
			g.setColor(bulletColor);
			g.fillOval(shooterX+width/8, (int)posy-height/16, width/4, height/8);
		}
	}
	private class Bullet{
		int width=5+RNG.nextInt(27), height=5+RNG.nextInt(27);
		double posx, posy;
		Color color=shooter.bulletColor;
		double speedx=5*color.getRed()/255.+.1;
		public Bullet(double posx, double posy){
			this.posx=posx;
			this.posy=posy;
		}
		public void draw(Graphics g){
			g.setColor(color);
			g.fillRect((int)posx-width/2, (int)posy-height/2, width, height);
		}
		public void update(){
			posx-=speedx;
		}
		public Rectangle2D getRect(){
			return new Rectangle2D.Double(posx-width/2, posy-height/2,width,height);
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public int getHeight() {
			return height;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		
	}

	public static void main(String[] args){
		new GeneticRacerEngine();
	}
}
