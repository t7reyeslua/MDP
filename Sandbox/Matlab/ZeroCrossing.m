f
%----------ZERO CROSSING----------------------------------------------------------

CeroSampleX=Sample(:,2)-m(1);
CeroSampleY=Sample(:,3)-m(2);
CeroSampleZ=Sample(:,4)-m(3);

CeroSampleN=NormalSample-mean(NormalSample);

% subplot(3,1,3)
% hold on;
% plot(CeroSampleX,'b');
% plot(CeroSampleY,'r');
% plot(CeroSampleZ,'g');
% plot(CeroSampleN,'black');
% hold off;
% title('Zeros');

nCross1=0;
nCross2=0;
nCross3=0;
nCrossN=0;

for i=(1:WindowSample-LowerLimit)
    
   if(((CeroSampleX(i)>0) & (CeroSampleX(i+1))<=0 )| ((CeroSampleX(i)<0) & (CeroSampleX(i+1))>=0 )) 
        nCross1=nCross1+1;
   end;
   
   if(((CeroSampleY(i)>0) & (CeroSampleY(i+1))<=0 )| ((CeroSampleY(i)<0) & (CeroSampleY(i+1))>=0 )) 
        nCross2=nCross2+1;
   end;
   
   if(((CeroSampleZ(i)>0) & (CeroSampleZ(i+1))<=0 )| ((CeroSampleZ(i)<0) & (CeroSampleZ(i+1))>=0 )) 
        nCross3=nCross3+1;
   end;
   
   
   if(((CeroSampleN(i)>0) & (CeroSampleN(i+1))<=0 )| ((CeroSampleN(i)<0) & (CeroSampleN(i+1))>=0 )) 
        nCrossN=nCrossN+1;
   end;
       
end;


maxPeriod=(Sample(WindowSample-LowerLimit,1)-Sample(1,1))/1000;


freq(1)= (nCross1/2)/maxPeriod;
freq(2)= (nCross2/2)/maxPeriod;
freq(3)= (nCross3/2)/maxPeriod;
freq;
Nfreq= (nCrossN/2)/maxPeriod;

mtot=m(1)+m(2)+m(3);
mabs=abs(m(1))+abs(m(2))+abs(m(3));

