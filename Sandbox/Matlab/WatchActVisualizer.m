% Author: Luis A. Gonzalez
% Date	: 13-10-2014
% Brief	: Matlab script. Sensors data vizualizer.
%% Working Path 
cd 'C:\Users\LG\Documents\GitHub\MDP\Sandbox\Matlab';
clearvars ;
clf( figure(1))
clf( figure(2))
clf( figure(3))

% Change the .txt to be readed
datasetName= 'log_SCE_20141014_233115.txt';
datasetspath  = 'C:\Users\LG\Dropbox\SENSORS\BrushT1';

resultsname='ResultsWatchTV.txt';
resultspath='C:\Users\LG\Documents\GitHub\MDP\Sandbox\Matlab';
%% Precision of the results Int and decimal point and FFT
pNum='4';
pDec='8';
fftN=1000; %biggger the more resolution
CutFreq=5;

%After a first run,check fig, change limits to desired sample to get std dev
LowerLimit=1; %Min 1
WindowSample=inf; %"inf" for using the whole set

%Declaring the Sensors
    GTitles = [ 'Acc       '; %1
                'Gyro      '; %2
                'Gravity   '; %3
                'Magnet    '; %4
                'Linear Acc'; %5
                'Tilt      '; %6
                'Rotation  ']; %7

ShowWhole='M_R';

Compare2=[1;2]; %Compare two sensors,see table avove, if [0;0] no graph is shown

Compare3=[6]; %Compare axis of a sensor

SaveFig=0;
%% Start Program
GSets=cellstr(GTitles);
UpperLimit=LowerLimit+WindowSample;

FileToRead=fullfile(datasetspath,datasetName);
fileID = fopen(FileToRead,'r');
fSpecX3=' %f %f %f';
fSpecX5=' %f %f %f %f %f';
%num(1),timestamp(1),Acc(3),gyro(3),gravity(3),magnet(3),linAcc(3),tilt(3),Rotation(5)
formatSpec = strcat('%d %f',fSpecX3,fSpecX3,fSpecX3,fSpecX3,fSpecX3,fSpecX3,fSpecX5,);
sizeM = [25 Inf];


% Matrix_R, contains the Wholedataset.
%% Create M_ Matrices
%Raw,Filtered,Zeromean,FastFurierTransform
%M_R, M_R_F, M_R_F_FFT,M_R_FFT
%M_R_Z, M_R_Z_Fi, M_R_Z_Fi_FFT,M_R_Z_FFT

M_R = fscanf(fileID,formatSpec,sizeM);
fclose(fileID);
M_R = M_R';  % Change to transpose

[Rrow,Rcol]=size(M_R);
%Validate Limits
if(size(M_R)<UpperLimit)
    LimitSize=size(M_R);
    UpperLimit=LimitSize(1);
end
SampleSize=Rrow;

Sampletime = datenumtosecs(M_R(SampleSize,2))-datenumtosecs(M_R(1,2));
if(Sampletime<0)
    Sampletime=datenumtosecs(M_R(SampleSize,2))+(240000000-datenumtosecs(M_R(1,2)));
end


SampleFreq=floor(SampleSize/(Sampletime));


M_R_Z=ZeroGraph(M_R);

M_R_F=ButtFilter(M_R,CutFreq,SampleFreq);
M_R_Z_F=ButtFilter(M_R_Z,CutFreq,SampleFreq);

M_R_FFT=fft_Graph(M_R,SampleFreq);
M_R_Z_FFT=fft_Graph(M_R_Z,SampleFreq);

nfft=SampleSize;
nyquist = SampleFreq/2;
plotfreq=(1:nfft/2)/(nfft/2)*nyquist;

Fundfreq= zeros(1,Rcol);
FundfreqZero = zeros(1,Rcol);
ZX_M_R = zeros(1,Rcol);
ZX_M_R_Z = zeros(1,Rcol);
ZX_M_R_F = zeros(1,Rcol);
ZX_M_R_Z_F=zeros(1,Rcol);
Hzerocross = dsp.ZeroCrossingDetector;
% 
% for c=(3:Rcol)
%     Fundfreq(:,c) = plotfreq(find(M_R_FFT(:,c)==max(M_R_FFT(:,c))));
%     FundfreqZero(:,c) = plotfreq(find(M_R_Z_FFT(:,c)==max(M_R_Z_FFT(:,c))));
%     
%     ZX_M_R(:,c) = step(Hzerocross,M_R(:,c));
%     ZX_M_R_Z(:,c) = step(Hzerocross,M_R_Z(:,c));
%     ZX_M_R_F(:,c) = step(Hzerocross,M_R_F(:,c));
%     ZX_M_R_Z_F(:,c) = step(Hzerocross,M_R_Z_F(:,c));
%     
% end

%%filtered fig
% clf( figure(4))
%     figure(4);
%     hold on;
%     for c=(3:6)
%     plot(M_R_F(:,c),'Color',[rand() rand() rand()]);
%     end
%     hold off;
%     title('Filtered!');

%% Vizualization
   switch(ShowWhole)
   case 'M_R' 
      M_Show=M_R;

   case 'M_R_Z' 
      M_Show=M_R_Z;

   case 'M_R_F' 
      M_Show=M_R_F;

   case 'M_R_Z_F'
      M_Show=M_R_Z_F;

   case 'M_R_FFT' 
      M_Show=M_R_FFT;

   case 'M_R_Z_FFT' 
      M_Show=M_R_Z_FFT;

   otherwise
     fprintf('Invalid case\n' );
   end
   
%% Fig 1, whole set
if(not(strcmp(ShowWhole,'')))
      
    figure(1);
    for g=(1:6)
        subplot(4,2,g)
        hold on;
        plot(M_Show(:,g*3),'b');
        plot(M_Show(:,g*3+1),'r');
        plot(M_Show(:,g*3+2),'green');
            hx = graph2d.constantline(LowerLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
            changedependvar(hx,'x');
            hx = graph2d.constantline(UpperLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
            changedependvar(hx,'x');
        hold off;
        title(GSets{g});
    end
        
        g=g+1;
            subplot(4,2,g)
        hold on;
        plot(M_Show(:,g*3),'b');
        plot(M_Show(:,g*3+1),'r');
        plot(M_Show(:,g*3+2),'green');
        plot(M_Show(:,g*3+3),'black');
        plot(M_Show(:,g*3+4),'yellow');
            hx = graph2d.constantline(LowerLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
            changedependvar(hx,'x');
            hx = graph2d.constantline(UpperLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
            changedependvar(hx,'x');
        hold off;
        title(GSets{g});
end  
        
%% Fig 2, Compare 2
if(Compare2~=0)
    figure(2);
    g=Compare2(1);
    
    subplot(2,1,1)
    hold on;
    plot(M_Show(LowerLimit:UpperLimit,g*3),'b');
    plot(M_Show(LowerLimit:UpperLimit,g*3+1),'r');
    plot(M_Show(LowerLimit:UpperLimit,g*3+2),'green');
    if(g==7)
        plot(M_Show(LowerLimit:UpperLimit,g*3+3),'yellow');
        plot(M_Show(LowerLimit:UpperLimit,g*3+4),'black');
    end
    hold off;
    title(GSets{g});
    
    g=Compare2(2);
    
    subplot(2,1,2)
    hold on;
    plot(M_Show(LowerLimit:UpperLimit,g*3),'b');
    plot(M_Show(LowerLimit:UpperLimit,g*3+1),'r');
    plot(M_Show(LowerLimit:UpperLimit,g*3+2),'green');
    if(g==7)
        plot(M_Show(LowerLimit:UpperLimit,g*3+3),'yellow');
        plot(M_Show(LowerLimit:UpperLimit,g*3+4),'black');
    end
    hold off;
    title(GSets{g});
end  

%% Fig 3, Compare same
if(Compare3~=0)
    figure(3);
    g=Compare3(1);
    gg=0;
    gr=0;
    
    if(g==6);
        gr=2;
    end
    
    if(g==7);
        gg=2;
    end
    
    for g=(1:3+gr)
        title('X-Blue,Y-Red,Z-Green');
        subplot(3+gr,1,g)
        hold on;
        plot(M_Show(:,g*3+g-1+gg),'b');
        hold off;
    end
        
end  

%% fig 4
% 
% clf( figure(4))
%     figure(4);
%     hold on;
%     for c=(21:25)
%     plot(M_R_F(:,c),'Color',[rand() rand() rand()]);
%     end
%     hold off;
%     title('Filtered!');
    
%% %Saving fig.
if(SaveFig==1)
    FigHandle = figure('Position', [100, 100, 1049, 895]);   
    figure(Showfft+Compare2+1); 
    hold on;
        plot(M_R(:,3),'b');
        plot(M_R(:,4),'r');
        plot(M_R(:,5),'green');
            hx = graph2d.constantline(LowerLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
            changedependvar(hx,'x');
            hx = graph2d.constantline(UpperLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
            changedependvar(hx,'x');
        hold off;
     printplot=figure(Showfft+Compare2+1);
     readfileName=strsplit(datasetName,'.');
     figName=char(readfileName(1));
     saveas(printplot, fullfile(resultspath,strcat(figName,'.png')),'png');
         close(figure(Showfft+Compare2+1));
end

    
%     figure(Showfft+ShowSample+1); 
% hold on;
%     plot(Matrix_R(:,3),'b');
%     plot(Matrix_R(:,4),'r');
%     plot(Matrix_R(:,5),'green');
%         hx = graph2d.constantline(LowerLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
%         changedependvar(hx,'x');
%         hx = graph2d.constantline(UpperLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
%         changedependvar(hx,'x');
%     hold off;


