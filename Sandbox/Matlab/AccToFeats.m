% Author: Luis A. Gonzalez
% Date	: 21-09-2014
% Brief	: Matlab script. Accel data to Features.
% Reads/store all dataset, calculates all features of the Sample only
% Start and Windowframe need to be set, change after first vizualization.
% Returns 'Results.txt'

clearvars -except EXA EXB EXC EXD;
clf

% Change the .txt to be readed

FileToRead='C:\Users\LG\Documents\GitHub\MDP\Sandbox\Matlab\Datasets\SS\Walking  1061018013.txt';

%Precision of the results Int and decimal point and FFT
pNum='4';
pDec='8';
fftN=200; %biggger the more resolution

%2) After a first run,check fig, change limits to desired sample to get std dev
LowerLimit=500; %Min 1
WindowSample=LowerLimit+1000;


fileID = fopen(FileToRead,'r');
formatSpec = '%d %f %f %f';
sizeM = [4 Inf];

% Matrix_R, contains the dataset.
Matrix_R = fscanf(fileID,formatSpec,sizeM);
fclose(fileID);

% Change transpose
Matrix_R = Matrix_R';  


%Vizualization
    figure(1);
    subplot(2,1,1)
    hold on;
    plot(Matrix_R(:,2),'b');
    plot(Matrix_R(:,3),'r');
    plot(Matrix_R(:,4),'black');
    hold off;
    title('ax,ay,az');

% All features 
Sample = zeros(WindowSample-LowerLimit+1,4);
for i=(1:WindowSample-LowerLimit+1)
    Sample(i,:) = Matrix_R(LowerLimit+1+i,:);
    
    NormalSample(i) = sqrt(power(Sample(i,2),2)+power(Sample(i,3),2)+power(Sample(i,4),2));
    

    
end;

% Time Domain Features
%----------------------------------
% Mean, Standard Deviation, Variance, Covariance and their Magnitudes (norm)
stdX=std(Sample(:,2));
stdY=std(Sample(:,3));
stdZ=std(Sample(:,4));

meanX=mean(Sample(:,2));
meanY=mean(Sample(:,3));
meanZ=mean(Sample(:,4));

varianceX=var(Sample(:,2));
varianceY=var(Sample(:,3));
varianceZ=var(Sample(:,4));

covXY=cov(Sample(:,2),Sample(:,3));
covYZ=cov(Sample(:,3),Sample(:,4));
covZX=cov(Sample(:,4),Sample(:,2));
    
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

% Plot sample
    subplot(2,1,2)
    hold on;
    plot(Matrix_R(LowerLimit:WindowSample,2),'b');
    plot(Matrix_R(LowerLimit:WindowSample,3),'r');
    plot(Matrix_R(LowerLimit:WindowSample,4),'black');
    plot(NormalSample,'green');
    hold off;
    title('Selection');


fileID = fopen('C:\Users\LG\Documents\GitHub\MDP\Sandbox\Matlab\Results.txt','w');
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

% Frequency Domain Features
%----------------------------------

fftF = [0:fftN-1]/fftN;

fftX=fft(Sample(:,2),fftN);
fftY=fft(Sample(:,3),fftN);
fftZ=fft(Sample(:,4),fftN);

ftplotX=fftshift(abs(fftX));
ftplotY=fftshift(abs(fftY));
ftplotZ=fftshift(abs(fftZ));

% Plot sample
    figure(2);
    subplot(3,1,1)
    plot(fftF,ftplotX,'b');
    subplot(3,1,2)
    plot(fftF,ftplotY,'r');
    subplot(3,1,3)
    plot(fftF,ftplotZ,'green');
    title('FFT');
