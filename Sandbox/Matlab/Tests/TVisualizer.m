% Author: Luis A. Gonzalez
% Date	: 25-09-2014
% Brief	: Matlab script. Accel data vizualizer.
% Reads dataset *.txt calculates all features of the Sample only


%%
clearvars ;
clf( figure(1))
clf( figure(2))

% Change the .txt to be readed
datasetName= 'log_walkFFT_20141005_202210.txt';
datasetspath  = 'C:\Users\LG\Dropbox\MDP_LAG\Galaxy nexus';

resultsname='Results.txt';
resultspath='C:\Users\LG\Documents\GitHub\MDP\Sandbox\Matlab';

%Precision of the results Int and decimal point and FFT
pNum='4';
pDec='8';
fftN=1000; %biggger the more resolution

%2) After a first run,check fig, change limits to desired sample to get std dev
LowerLimit=1; %Min 1
WindowSample=inf; %"inf" for using the whole set

%Showgraphs:
ShowSample=1;
Showfft=1;
SaveFig=0;

%% Start TVizualize

UpperLimit=LowerLimit+WindowSample;

FileToRead=fullfile(datasetspath,datasetName);
fileID = fopen(FileToRead,'r');
formatSpec = '%d %f %f %f %f';
sizeM = [5 Inf];


% Matrix_R, contains the dataset.
Matrix_R = fscanf(fileID,formatSpec,sizeM);
fclose(fileID);

% Change transpose

Matrix_R = Matrix_R';  
if(size(Matrix_R)<UpperLimit)
    LimitSize=size(Matrix_R);
    UpperLimit=LimitSize(1);
end

SampleSize=UpperLimit-LowerLimit;

%% Vizualization

%% Fig 1, whole set
    figure(1);
    subplot(2,1,1)
    hold on;
    plot(Matrix_R(:,3),'b');
    plot(Matrix_R(:,4),'r');
    plot(Matrix_R(:,5),'green');
        hx = graph2d.constantline(LowerLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
        changedependvar(hx,'x');
        hx = graph2d.constantline(UpperLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
        changedependvar(hx,'x');
    hold off;
    title('ax-Blue,ay-Red,az-Green');

% All features 

SampleTV = zeros(UpperLimit-LowerLimit+1,4);
for i=(1:UpperLimit-LowerLimit)
    SampleTV(i,1) = Matrix_R(LowerLimit+i,2);
    SampleTV(i,2) = Matrix_R(LowerLimit+i,3);
    SampleTV(i,3) = Matrix_R(LowerLimit+i,4);
    SampleTV(i,4) = Matrix_R(LowerLimit+i,5);
    
    NormalSample(i) = sqrt(power(SampleTV(i,2),2)+power(SampleTV(i,3),2)+power(SampleTV(i,4),2));  
end;

% Time Domain Features
%----------------------------------
% Mean, Standard Deviation, Variance, Covariance and their Magnitudes (norm)
stdX=std(SampleTV(:,2));
stdY=std(SampleTV(:,3));
stdZ=std(SampleTV(:,4));

meanX=mean(SampleTV(:,2));
meanY=mean(SampleTV(:,3));
meanZ=mean(SampleTV(:,4));

varianceX=var(SampleTV(:,2));
varianceY=var(SampleTV(:,3));
varianceZ=var(SampleTV(:,4));

covXY=cov(SampleTV(:,2),SampleTV(:,3));
covYZ=cov(SampleTV(:,3),SampleTV(:,4));
covZX=cov(SampleTV(:,4),SampleTV(:,2));
    
%regression line of Covariance relation: y=mx +b
% http://facultyweb.berry.edu/vbissonnette/statshw/doc/reg_sup.html
    Reg_XY_b=covXY/varianceX;   
    Reg_XY_a=meanY-Reg_XY_b*meanX;

    Reg_YZ_b=covYZ/varianceY;
    Reg_YZ_a=meanZ-Reg_YZ_b*meanY;

    Reg_ZX_b=covZX/varianceZ;
    Reg_ZX_a=meanX-Reg_ZX_b*meanZ;

mMagnitude=sqrt(power(meanX,2)+power(meanY,2)+power(meanZ,2));
stdMagnitude=sqrt(power(stdX,2)+power(stdY,2)+power(stdZ,2));
varMagnitude=sqrt(power(varianceX,2)+power(varianceY,2)+power(varianceZ,2));

%% Fig 2, Sample
if(ShowSample==1)
    % Plot sample
    subplot(2,1,2)
    hold on;
    plot(Matrix_R(LowerLimit:UpperLimit,3),'b');
    plot(Matrix_R(LowerLimit:UpperLimit,4),'r');
    plot(Matrix_R(LowerLimit:UpperLimit,5),'black');
    plot(NormalSample,'green');
    hold off;
    title('Selection');
else
    close(figure(1));
end  



fileID = fopen(fullfile(resultspath,resultsname),'w');
fprintf(fileID,'%s\t','File');
fprintf(fileID,'%s\t%s\t%s\t','mean X','mean Y','mean Z');
fprintf(fileID,'%s\t%s\t%s\t','std X','std Y','std Z');
fprintf(fileID,'%s\t%s\t%s\t','var X','var Y','var Z');
fprintf(fileID,'%s\t%s\t%s\t','Mean Mag','Std Mag','Var Mag');
fprintf(fileID,'%s\t%s\t%s\t','cov XY(1,1)','cov XY(1,2)','cov XY(2,1)','cov XY(2,2)');
fprintf(fileID,'%s\t%s\t%s\t','cov YZ(1,1)','cov YZ(1,2)','cov YZ(2,1)','cov YZ(2,2)');
fprintf(fileID,'%s\t%s\t%s\t','cov ZX(1,1)','cov ZX(1,2)','cov ZX(2,1)','cov ZX(2,2)');
fprintf(fileID,'\n');


precisionprint=strcat('%',pNum,'.',pDec,'f\t','%',pNum,'.',pDec,'f\t','%',pNum,'.',pDec,'f\t');
% precisionprint='%4.4f\t%4.4f\t%4.4f\t';

fprintf(fileID,'%s\t','Results');
fprintf(fileID,precisionprint,meanX,meanY,meanZ);
fprintf(fileID,precisionprint,stdX,stdY,stdZ);
fprintf(fileID,precisionprint,varianceX,varianceY,varianceZ);
fprintf(fileID,precisionprint,mMagnitude,stdMagnitude,varMagnitude);
fprintf(fileID,precisionprint,covXY(1,1),covXY(2,1),covXY(1,2),covXY(2,2));
fprintf(fileID,precisionprint,covXY(1,1),covXY(2,1),covXY(1,2),covXY(2,2));
fprintf(fileID,precisionprint,covXY(1,1),covXY(2,1),covXY(1,2),covXY(2,2));
fprintf(fileID,'\n');

fclose(fileID);


%% Sample timefreq properties
Sampletime=SampleTV(SampleSize-1,1)-SampleTV(1,1);%in miliseconds

SampleFreq=floor(SampleSize/(Sampletime/1000));


% Frequency Domain Features
%----------------------------------
if(Showfft==1)
    
    

    NFFT = 2^nextpow2(SampleSize);

    fftX=fft(SampleTV(:,2));
    fftX(1)=[];
    fftY=fft(SampleTV(:,3));
    fftY(1)=[];
    fftZ=fft(SampleTV(:,4));
    fftZ(1)=[];

    nfft=length(fftX);
    powerFFTX = abs(fftX(1:floor(nfft/2))).^2;
    powerFFTY = abs(fftY(1:floor(nfft/2))).^2;
    powerFFTZ = abs(fftZ(1:floor(nfft/2))).^2;
    
    nyquist = SampleFreq/2;
    plotfreq=(1:nfft/2)/(nfft/2)*nyquist;

    FundfreqX = plotfreq(find(powerFFTX==max(powerFFTX)));
    FundfreqY = plotfreq(find(powerFFTY==max(powerFFTY)));
    FundfreqZ = plotfreq(find(powerFFTZ==max(powerFFTZ)));
    
    
    Yaxis=max(max(max(powerFFTX),max(powerFFTY)),max(powerFFTZ));


%Fig FFT

    % Plot sample
        Xaxis=10;
        figure(ShowSample+1);
        hold on;
        
        subplot(3,1,1)
        plot(plotfreq,powerFFTX,'b');
        axis([0,Xaxis,0,Yaxis])
        title('fft Acc X');
             hx = graph2d.constantline((FundfreqX), 'LineStyle',':', 'Color','b');
             changedependvar(hx,'x');
        
        subplot(3,1,2)
        plot(plotfreq,powerFFTY,'r');
        axis([0,Xaxis,0,Yaxis])
        title('fft Acc Y');
        hx = graph2d.constantline((FundfreqY), 'LineStyle',':', 'Color','r');
             changedependvar(hx,'x');
        
        subplot(3,1,3)
        plot(plotfreq,powerFFTZ,'green');
        axis([0,Xaxis,0,Yaxis])
        title('fft Acc Z');
        hx = graph2d.constantline((FundfreqZ), 'LineStyle',':', 'Color','green');
             changedependvar(hx,'x');
        
        hold off;
else
    close(figure(ShowSample+1));
end       

%% Filter fig
cutfreq=2.5;
fNorm = cutfreq / (SampleFreq/2);
[b,a] = butter(2, fNorm, 'low');
SampleMean=SampleTV;
SampleMean(:,2)=SampleTV(:,2)-meanX;
SampleMean(:,3)=SampleTV(:,3)-meanY;
SampleMean(:,4)=SampleTV(:,4)-meanZ;
FilterSample = filtfilt(b, a, SampleMean);

figure(2);
hold on;
plot(FilterSample(:,2),'b');
plot(FilterSample(:,3),'r');
plot(FilterSample(:,4),'green');
hold off;
title('resample 15 ');

%% Zero crossing 
Hzerocross = dsp.ZeroCrossingDetector;
ZeroCrossX = step(Hzerocross,SampleMean(:,2))
ZeroCrossY = step(Hzerocross,SampleMean(:,3))
ZeroCrossZ = step(Hzerocross,SampleMean(:,4))

FZeroCrossX = step(Hzerocross,SampleMean(:,2))
FZeroCrossY = step(Hzerocross,SampleMean(:,3))
FZeroCrossZ = step(Hzerocross,SampleMean(:,4))

%% %Saving fig.
if(SaveFig==1)
    FigHandle = figure('Position', [100, 100, 1049, 895]);   
    figure(Showfft+ShowSample+1); 
    hold on;
        plot(Matrix_R(:,3),'b');
        plot(Matrix_R(:,4),'r');
        plot(Matrix_R(:,5),'green');
            hx = graph2d.constantline(LowerLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
            changedependvar(hx,'x');
            hx = graph2d.constantline(UpperLimit, 'LineStyle',':', 'Color',[.7 .7 .7]);
            changedependvar(hx,'x');
        hold off;
     printplot=figure(Showfft+ShowSample+1);
     readfileName=strsplit(datasetName,'.');
     figName=char(readfileName(1));
     saveas(printplot, fullfile(resultspath,strcat(figName,'.png')),'png');
         close(figure(Showfft+ShowSample+1));
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


