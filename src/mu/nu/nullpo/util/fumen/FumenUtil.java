package mu.nu.nullpo.util.fumen;

import java.util.HashMap;

import org.apache.log4j.Logger;

import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.mode.NetVSBattleMode;

public class FumenUtil {
	
	/** Log */
	static final Logger log = Logger.getLogger(FumenUtil.class);
	
	//** Belzebub mod vars **//
	private int framesAdded = 0;
	
	private FumenState state = FumenState.WAITING;
	
	//** FUMEN ENCODING VARS**
	private int numberOffFieldsInPlayField = 220;
	
	private int framelim = 18000; //2000; // frame limit ~*9, came prolly from 2048
	private int enclim=294912;			  //32768; 	// Limit number of characters encoded ~*9
	private int framemax=0;	    // 'maximum frame number', numbers of frames in use it seems
	
	private int[] enc= new int[enclim+1024]; // Encoded sequence
	private String enctbl="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"; 
	private String asctbl=" !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"; // ASCII character table
	
	 int[]b = new int[]{ //Piece pattern
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,1,1,1,2,1,3,1,1,0,1,1,1,2,1,3,0,1,1,1,2,1,3,1,1,0,1,1,1,2,1,3,
			0,1,1,1,2,1,0,2,1,0,1,1,1,2,2,2,2,0,0,1,1,1,2,1,0,0,1,0,1,1,1,2,
			1,1,2,1,1,2,2,2,1,1,2,1,1,2,2,2,1,1,2,1,1,2,2,2,1,1,2,1,1,2,2,2,
			0,1,1,1,1,2,2,2,2,0,1,1,2,1,1,2,0,1,1,1,1,2,2,2,2,0,1,1,2,1,1,2,
			0,1,1,1,2,1,1,2,1,0,1,1,2,1,1,2,1,0,0,1,1,1,2,1,1,0,0,1,1,1,1,2,
			0,1,1,1,2,1,2,2,1,0,2,0,1,1,1,2,0,0,0,1,1,1,2,1,1,0,1,1,0,2,1,2,
			1,1,2,1,0,2,1,2,0,0,0,1,1,1,1,2,1,1,2,1,0,2,1,2,0,0,0,1,1,1,1,2
	};
	
	private String cmstrrep="";
	
	private int ct=1; //color 色
	
	//Virtual Field
	private  int[] af = new int[220*(framelim+1)];    //all frames
	private  int[] f = new int[220];					//current frame
	private  int[] encf = new int[220];

	//Type Peace, angles, coordinate
	private  int[] ap = new int[3*(framelim+1)];		//all pieces in game
	private  int[] p = new int[3];					//current piece
	
	private  int[] au=new int[framelim]; // Bidding up
	private  int[] am=new int[framelim]; // Reversing switch
	private  String[] ac= new String[framelim]; // Comment
	private  int[] ad=new int[framelim]; // Adhesion

	private static HashMap<GameEngine, FumenUtil> fumenUtils = new HashMap<GameEngine, FumenUtil>();
	public static FumenUtil getFumenUtil(GameEngine engine){
		if (fumenUtils.get(engine) == null){
			fumenUtils.put(engine, new FumenUtil());
		}
		return fumenUtils.get(engine);
	}
	
	private static HashMap<GameEngine, FumenUtil> fullFrameFumenUtils = new HashMap<GameEngine, FumenUtil>();
	public static FumenUtil getFullFrameFumenUtil(GameEngine engine){
		if (fullFrameFumenUtils.get(engine) == null){
			fullFrameFumenUtils.put(engine, new FumenUtil());
		}
		return fullFrameFumenUtils.get(engine);
	}
	
	public void start(){
		if (state != FumenState.ACTIVE){
			log.debug("Fumen recording started");
			this.state = FumenState.ACTIVE;
		}
	}
	
	public void end(){
		if (state != FumenState.ENDED){
			log.debug("Fumen recording ended");
			this.state = FumenState.ENDED;
		}
	}
	
	public void reset(){
		
		state = FumenState.WAITING;
		framesAdded = 0;
		
		//Virtual Field
		af = new int[220*(framelim+1)];    //all frames
		f = new int[220];					//current frame
		encf = new int[220];

		//Type Peace, angles, coordinate
		ap = new int[3*(framelim+1)];		//all pieces in game
		p = new int[3];					//current piece
		
		au=new int[framelim]; // Bidding up
		am=new int[framelim]; // Reversing switch
		ac= new String[framelim]; // Comment
		ad=new int[framelim]; // Adhesion
	}
	
	public void addFrame(int[] someNewFrame){
		if (isActive()){
			for (int pfi = 0; pfi < 220; pfi++){
		        af[framesAdded * 220 + pfi]= someNewFrame[pfi];
			}
		    framesAdded++;
		    framemax = framesAdded;
		}
	}
	
	public void addFrameAndActivePiece(int[] someNewFrame, int[] activePiece){
		if (isActive()){
			addFrame(someNewFrame);
		    for (int pfi = 0; pfi < 3; pfi++){
		    	if (framesAdded > 1){
		    		ap[(framesAdded-2) * 3 + pfi] = activePiece[pfi];
		    	}
		    }
		} 
	}

	private boolean isActive() {
		return state == FumenState.ACTIVE;
	}
	
	public String encode() {
		    
		    int encc = 0; //encryption counter
		    int fldrepaddr = -1;
		    for (int i = 0; i < 220; i++)
		        encf[i] = 0;
		    for (int e = 0; encc < enclim && e <= framemax; e++) {
		        // Field output
		        for (int j = 0; j < 220; j++)
		            encf[j] = af[e * 220 + j] + 8 - encf[j];
		        int fc = 0;
		        for (int j = 0; j < 219; j++) {
		            fc++;
		            if (encf[j] != encf[j + 1]) {
		                double temp = encf[j] * 220 + (fc - 1);
		                enc[encc++] = (int) (temp % 64);
		                temp = Math.floor(temp / 64);
		                enc[encc++] = (int) (temp % 64);
		                temp = Math.floor(temp / 64);
		                fc = 0;
		            }
		        }
		        fc++;
		        int tmp = encf[219] * 220 + (fc - 1);
		        enc[encc++] = tmp % 64;
		        tmp = (int) Math.floor(tmp / 64);
		        enc[encc++] = tmp % 64;
		        tmp = (int) Math.floor(tmp / 64);
		        fc = 0;
		        if (enc[encc - 2] + enc[encc - 1] * 64 == 1979) {
		            if (fldrepaddr < 0) {
		                fldrepaddr = encc++;
		                enc[fldrepaddr] = 0;
		            } else {
		                if (enc[fldrepaddr] < 63) {
		                    enc[fldrepaddr]++;
		                    encc -= 2;
		                } else {
		                    fldrepaddr = encc++;
		                    enc[fldrepaddr] = 0;
		                }
		            }
		        } else {
		            if (fldrepaddr >= 0)
		                fldrepaddr = -1;
		        }
		        // Type, angle, coordinate output
		        cmstrrep = (e > 0) ? ac[e - 1] : "";
		        //refreshquiz(e, 1, 0);
		        
		        //cmstrrep = cmstrrep.substring(0, 4095); //cmstrrep = escape(cmstrrep).substring(0, 4095);
		        String tmpstr = ac[e]; //.substring(0, 4095); //tmpstr = escape(ac[e]).substring(0, 4095);
		       
		        int tmplen = tmpstr == null ? 0 : tmpstr.length();
		        if (tmplen > 4095)
		            tmplen = 4095;
		        int someBoolean = (e == 0) ? ct : 0;
				int someOtherBoolean = (tmpstr != cmstrrep) ? 56320 : 0;
				
				 tmp = ap[e * 3 + 0] + ap[e * 3 + 1] * 8 + ap[e * 3 + 2] * 32 + au[e] * 7040 + am[e] * 14080 + someBoolean * 28160 + someOtherBoolean + ad[e] * 112640;
		        enc[encc++] = tmp % 64;
		        tmp = (int) Math.floor(tmp / 64);
		        enc[encc++] = tmp % 64;
		        tmp = (int) Math.floor(tmp / 64);
		        enc[encc++] = tmp % 64;
		        tmp = (int) Math.floor(tmp / 64);
		        // コメント出力
		        if (tmpstr != cmstrrep) {
		            tmp = tmplen;
		            enc[encc++] = tmp % 64;
		            tmp = (int) Math.floor(tmp / 64);
		            enc[encc++] = tmp % 64;
		            tmp = (int) Math.floor(tmp / 64);
		            for (int i = 0; i < tmplen; i += 4) {
		            	int ins;
		                tmp = (((ins = asctbl.indexOf(tmpstr.charAt(i + 0))) >= 0 ? ins : 0) % 96);
		                tmp += (((ins = asctbl.indexOf(tmpstr.charAt(i + 1))) >= 0 ? ins : 0) % 96) * 96;
		                tmp += (((ins = asctbl.indexOf(tmpstr.charAt(i + 2))) >= 0 ? ins : 0) % 96) * 9216;
		                tmp += (((ins = asctbl.indexOf(tmpstr.charAt(i + 3))) >= 0 ? ins : 0) % 96) * 884736;
		                enc[encc++] = tmp % 64;
		                tmp = (int) Math.floor(tmp / 64);
		                enc[encc++] = tmp % 64;
		                tmp = (int) Math.floor(tmp / 64);
		                enc[encc++] = tmp % 64;
		                tmp = (int) Math.floor(tmp / 64);
		                enc[encc++] = tmp % 64;
		                tmp = (int) Math.floor(tmp / 64);
		                enc[encc++] = tmp % 64;
		                tmp = (int) Math.floor(tmp / 64);
		            }
		        }
		        // Storage field
		        for (int j = 0; j < 220; j++)
		            encf[j] = af[e * 220 + j];
		        if (!(ad[e]==1)) { //adhesion mino
		            // Block placement
		            if (ap[e * 3 + 0] > 0) {
		                for (int j = 0; j < 4; j++)
		                    encf[ap[e * 3 + 2] + b[ap[e * 3 + 0] * 32 + ap[e * 3 + 1] * 8 + j * 2 + 1] * 10 + b[ap[e * 3 + 0] * 32 + ap[e * 3 + 1] * 8 + j * 2] - 11] = ap[e * 3 + 0];
		            }
		            // Clear field shift
		            int someResuableI = 0;
		            for (int i = 20, k = 20; k >= 0; k--) {
		                int chk = 0;
		                for (int j = 0; j < 10; j++)
		                    chk += (encf[k * 10 + j] > 0) ? 1 : 0;
		                if (chk < 10) {
		                    for (int j = 0; j < 10; j++)
		                        encf[i * 10 + j] = encf[k * 10 + j];
		                    i--;
		                }
		                someResuableI = i;
		            }
		            for (int i = someResuableI; i >= 0; i--)
		                for (int j = 0; j < 10; j++)
		                    encf[i * 10 + j] = 0;
		            // Bidding up
		            if (au[e] == 1) {
		                for (int i = 0; i < 210; i++)
		                    encf[i] = encf[i + 10];
		                for (int i = 210; i < 220; i++)
		                    encf[i] = 0;
		            }
		            // Activate mirror
		            if (am[e] == 1)
		                for (int i = 0; i < 21; i++)
		                    for (int j = 0; j < 5; j++) {
		                        tmp = encf[i * 10 + j];
		                        encf[i * 10 + j] = encf[i * 10 + 9 - j];
		                        encf[i * 10 + 9 - j] = tmp;
		                    }
		        }
		    }
		    String encstr = "v110@";
		    for (int i = 0; i < encc; i++) {
		        encstr = encstr + enctbl.charAt(enc[i]);
		        if (i % 47 == 41)
		            encstr = encstr + "?";
		    }
		    //tx.value = encstr;
		    System.out.println("texfield value would be: " + encstr);
		    
		    String urlValue = "http://fumen.zui.jp/" + '?' + encstr;
		    String viewValue = "http://fumen.zui.jp/?m" + encstr.substring(1);
		    
		    System.out.println("url value:" + urlValue);
		    System.out.println("view value:" + viewValue);
		   
		    //refreshquiz(frame, 0, 1);

		    return encstr;
		}
	
	 void pushframe(int pfframe) {
	    for (int pfi = 0; pfi < 220; pfi++)
	        af[pfframe * 220 + pfi]= f[pfi];
	    for (int pfi = 0; pfi < 3; pfi++)
	        ap[pfframe * 3 + pfi] = p[pfi];
	}
	
//
//	function refreshquiz(rqframe,newpage,gui){ // クイズ
//	  var qfld=document.getElementById("qfld");
//	  var cm=document.getElementById("cm");
//	  var mode=document.getElementsByName("mode");
//	  var dc=document.getElementById("dc");
//	  cmstr=gui?cm.value:cmstrrep;
//	  if(cmstr.substring(0,3)=='#Q='){
//	    quiz=1;
//	    quizcur=-1;
//	    quizhld=-1;
//	    quiznx[0]=-1;
//	    quiznx[1]=-1;
//	    quiznx[2]=-1;
//	    quizcm="";
//	    qstr="";
//	    qlen=cmstr.length;
//	    for(i=3;i<qlen;i++){
//	      qca=cmstr.charAt(i);
//	      if(qca==';'){quizcm=';'+cmstr.substring(i+1);break;}
//	      if(quiztbl.indexOf(qca)>=0){qstr+=qca;}
//	    }
//	    qlen=qstr.length;
//	    qinstr1=qstr.indexOf('(');
//	    qinstr2=qstr.indexOf(')');
//	    if(qinstr1>=0&&qinstr2==qinstr1+2){
//	      quizcur=quiztbl.indexOf(qstr.charAt(qinstr1+1));
//	      if(quizcur<0||quizcur>6){
//	        quizcur=-1;
//	      }else{
//	        qstr=qstr.substring(0,qinstr1)+qstr.substring(qinstr2);
//	      }
//	    }
//	    qinstr1=qstr.indexOf('[');
//	    qinstr2=qstr.indexOf(']');
//	    if(qinstr1>=0&&qinstr2==qinstr1+2){
//	      quizhld=quiztbl.indexOf(qstr.charAt(qinstr1+1));
//	      if(quizhld<0||quizhld>6){
//	        quizhld=-1;
//	      }else{
//	        qstr=qstr.substring(0,qinstr1)+qstr.substring(qinstr2);
//	      }
//	    }
//	    qnxstr="";
//	    for(i=0;i<qlen;i++){
//	      qca=qstr.charAt(i);
//	      if(quiztbl.indexOf(qca)<=6){qnxstr+=qca;}
//	    }
//	    qlen=qnxstr.length;
//	    for(i=0;i<qlen&&i<3;i++){
//	      quiznx[i]=quiztbl.indexOf(qnxstr.charAt(i));
//	    }
//	    if(newpage&&!ad[rqframe-1]){
//	      if(ap[(rqframe-1)*3]-1==quizcur){
//	        quizcur=quiznx[0];
//	        quiznx[0]=quiznx[1];
//	        quiznx[1]=quiznx[2];
//	        qnxstr=qnxstr.substring(1);
//	      }else if(quizhld>=0&&ap[(rqframe-1)*3]-1==quizhld){
//	        quizhld=quizcur;
//	        quizcur=quiznx[0];
//	        quiznx[0]=quiznx[1];
//	        quiznx[1]=quiznx[2];
//	        qnxstr=qnxstr.substring(1);
//	      }else if(quizhld<0&&ap[(rqframe-1)*3]-1==quiznx[0]){
//	        quizhld=quizcur;
//	        quizcur=quiznx[1];
//	        qnxstr=qnxstr.substring(2);
//	      }
//	      if(quizcur>=0){
//	        cmstr='#Q=['+quiztbl.charAt(quizhld)+']('+quiztbl.charAt(quizcur)+')'+qnxstr+quizcm;
//	        if(gui){
//	          cm.value=cmstr;
//	        }else{
//	          cmstrrep=cmstr;
//	        }
//	      }else{
//	        cmstr=''+quizcm.substring(1);
//	        if(gui){
//	          cm.value=cmstr;
//	        }else{
//	          cmstrrep=cmstr;
//	        }
//	        quiz=0;
//	        quizcur=-1;
//	        quizhld=-1;
//	        quiznx[0]=-1;
//	        quiznx[1]=-1;
//	        quiznx[2]=-1;
//	      }
//	      refreshquiz(rqframe,0,gui);
//	      gui=0;
//	    }
//	    if(gui){
//	      for(j=0;j<7;j++){
//	        for(i=0;i<4;i++){
//	          mode[8+j*4+i].disabled=(!quiz||j==quizcur||j==(quizhld>=0?quizhld:quiznx[0]))?false:true;
//	        }
//	      }
//	      if(!ad[rqframe-1]){
//	        if(quizcur>=0){
//	          mode[8+quizcur*4+ct*2].checked=true;
//	        }else if(quizhld>=0){
//	          mode[8+quizhld*4+ct*2].checked=true;
//	        }else{
//	          mode[36].checked=true;
//	        }
//	      }
//	      for(i=0;i<86;i++){qf[i]="#000000";}
//	      if(quizhld>=0){
//	        dnt=quizhld+1;
//	        dnid=(ct&&(dnt==2||dnt==5||dnt==6));dnr=ct*2;
//	        for(i=0;i<4;i++){
//	          qf[(b[dnt*32+dnr*8+i*2+1]+dnid-1)*43+b[dnt*32+dnr*8+i*2]+0]=c[0+ct*27+dnt];
//	        }
//	      }
//	      if(quizcur>=0){
//	        dnt=quizcur+1;
//	        dnid=(ct&&(dnt==2||dnt==5||dnt==6));dnr=ct*2;
//	        for(i=0;i<4;i++){
//	          qf[(b[dnt*32+dnr*8+i*2+1]+dnid-1)*43+b[dnt*32+dnr*8+i*2]+7]=c[9+ct*27+dnt];
//	        }
//	      }
//	      for(j=0;j<6;j++){
//	        qca=qnxstr.charAt(j);
//	        qnx=quiztbl.indexOf(qca);
//	        if(qca!=''&&qnx>=0){
//	          dnt=qnx+1;
//	          dnid=(ct&&(dnt==2||dnt==5||dnt==6));dnr=ct*2;
//	          for(i=0;i<4;i++){
//	            qf[(b[dnt*32+dnr*8+i*2+1]+dnid-1)*43+b[dnt*32+dnr*8+i*2]+14+j*5]=c[0+ct*27+dnt];
//	          }
//	        }
//	      }
//	      qfstr="";
//	      qfstr+='<table width=184 height=12 border=0 cellspacing=0 cellpadding=0><td align=center valign=center style="background-color:#000000;">';
//	      qfstr+='<table align=center border=0 cellspacing=0 cellpadding=0>';
//	      for(j=0;j<2;j++){
//	        qfstr+='<tr>';
//	        for(i=0;i<43;i++){
//	          qfstr+='<td width=4 height=4 style="background-color:'+qf[j*43+i]+';"></td>';
//	        }
//	        qfstr+='</tr>';
//	      }
//	      qfstr+='</table>';
//	      qfstr+='</td></table>';
//	      if(qfld!=null){qfld.innerHTML=qfstr;}
//	    }
//	  }else{
//	    quiz=0;
//	    quizcur=-1;
//	    quizhld=-1;
//	    quiznx[0]=-1;
//	    quiznx[1]=-1;
//	    quiznx[2]=-1;
//	    if(gui){
//	      for(j=0;j<7;j++){
//	        for(i=0;i<4;i++){
//	          mode[8+j*4+i].disabled=false;
//	        }
//	      }
//	      qfstr="";
//	      qfstr+='<table width=184 height=12 border=0 cellspacing=0 cellpadding=0><td align=center valign=center style="background-color:#ffffff;">';
//	      qfstr+='</td></table>';
//	      if(qfld!=null){qfld.innerHTML=qfstr;}
//	    }
//	  }
//	  keybuttonenable(p[0]>0);
//	}
	
}
