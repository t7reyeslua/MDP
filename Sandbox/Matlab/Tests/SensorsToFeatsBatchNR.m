% Author: Luis A. Gonzalez
% Date	: 16-10-2014
% Brief	: Reads all the .txt files of a folder and gives back the .arff
% Only takes Sensors of smartwatch
clearvars 

%% Settings
    %% File Handling 
    % Folder of Sets must not containother than Sets,
    %the data should be  '%d %d %f %f %f ...'

    datasetspath  = 'C:\Users\LG\Dropbox\SENSORS\BrushT1';
    resultsname='WekaTBrushResults2.arff';
    resultspath='C:\Users\LG\Dropbox\SENSORS\';
    
    %% Precision of Other settings
    pNum='4';
    pDec='4';
    fftN=500; % FFT resolution biggger the more resolution

    % Sample window
    LowerLimit=1; %Min 1
    WindowSize=200; %"inf" for using the whole set
    
    %Filter properties
    cutfreq=2.6; %Cutt off frequency (low pass Butterworth filter)
    
    %% Features to enable (1 is Enable)
    ftTDomain=1;        %mean 3x, std dev 3x, variance 3,x & Magnitudes of this values
    ftTCovariance=0;    %2x2 matrices of covariance relations xy,yz,zx.
    ftfft=1;            %fast furier transform
    ftfilter=1;         %filter
    
    %% Sensors to enable
    % number of active sensors (see ref )
    % Example: just Tilt and Gyro: ActSen=[5,2];
    % Accelerometer=1
    % Gyroscope    =2
    % Magnetometer =3
    % Linear Acc   =4
    % Tilt         =5
    % Rotation     =6
    ActSen=[1,2,3,4,5,6]; 
    
    %% arff Notes; write any coments here
FileComments='This is a 1 line comment';

%% PROGRAM STARTS

    %% Writting header in Results


Filelist    = dir(fullfile(datasetspath, '*.txt'));
nFiles   = length(Filelist);

resultgo = fullfile(resultspath,resultsname);
fileID = fopen(resultgo,'w');
fprintf(fileID,'%s\t%s\n\n','%',FileComments);
fprintf(fileID,'%s\n\n','@relation Monitoring');

%%Namming Sensors

    GTitles = [ 'Acc'; %1
                'Gyr'; %2
                'Mag'; %3
                'LAc'; %4
                'Til'; %5
                'Rot']; %6
    GSets=cellstr(GTitles);
    
%     ActSen=ActSen+2;%to skip timestamp and number
    SizeActSen=size(ActSen);
    SizeActSen(2);

    %% Print @attributes

for nsensors=(1:SizeActSen(2))
    sensortoread=ActSen(nsensors);
    if(ftTDomain==1)
        fprintf(fileID,'%s\n%s\n%s\n',strcat('@attribute',' meanX_',GSets{sensortoread},' numeric'),strcat('@attribute',' meanY_',GSets{sensortoread},' numeric'),strcat('@attribute',' meanZ_',GSets{sensortoread},' numeric'));
        if(sensortoread==7)
            fprintf(fileID,'%s\n%s\n',strcat('@attribute',' meanA_',GSets{sensortoread},' numeric'),strcat('@attribute',' meanB_',GSets{sensortoread},' numeric'));
        end
        
        fprintf(fileID,'%s\n%s\n%s\n',strcat('@attribute',' stdX_',GSets{sensortoread},' numeric'),strcat('@attribute',' stdY_',GSets{sensortoread},' numeric'),strcat('@attribute',' stdZ_',GSets{sensortoread},' numeric'));
        if(sensortoread==7)
            fprintf(fileID,'%s\n%s\n',strcat('@attribute',' stdA_',GSets{sensortoread},' numeric'),strcat('@attribute',' stdB_',GSets{sensortoread},' numeric'));
        end
        
        fprintf(fileID,'%s\n%s\n%s\n',strcat('@attribute',' varX_',GSets{sensortoread},' numeric'),strcat('@attribute',' varY_',GSets{sensortoread},' numeric'),strcat('@attribute',' varZ_',GSets{sensortoread},' numeric'));
        if(sensortoread==7)
            fprintf(fileID,'%s\n%s\n',strcat('@attribute',' varA_',GSets{sensortoread},' numeric'),strcat('@attribute',' varB_',GSets{sensortoread},' numeric'));
        end
        
        fprintf(fileID,'%s\n%s\n%s\n',strcat('@attribute',' MeanMag_',GSets{sensortoread},' numeric'),strcat('@attribute',' StdMag_',GSets{sensortoread},' numeric'),strcat('@attribute',' VarMag_',GSets{sensortoread},' numeric'));
    else
    end

%     if(ftTCovariance==1)
%         fprintf(fileID,'%s\n%s\n%s\n%s\n','@attribute covXY(1-1) numeric','@attribute covXY(1-2) numeric','@attribute covXY(2-1) numeric','@attribute covXY(2-2) numeric');
%         fprintf(fileID,'%s\n%s\n%s\n%s\n','@attribute covYZ(1-1) numeric','@attribute covYZ(1-2) numeric','@attribute covYZ(2-1) numeric','@attribute covYZ(2-2) numeric');
%         fprintf(fileID,'%s\n%s\n%s\n%s\n','@attribute covZX(1-1) numeric','@attribute covZX(1-2) numeric','@attribute covZX(2-1) numeric','@attribute covZX(2-2) numeric');
%     end

    if(ftfft==1)
       fprintf(fileID,'%s\n%s\n%s\n',strcat('@attribute',' FundFreqX_',GSets{sensortoread},' numeric'),strcat('@attribute',' FundFreqY_',GSets{sensortoread},' numeric'),strcat('@attribute',' FundFreqZ_',GSets{sensortoread},' numeric'));
        if(sensortoread==7)
            fprintf(fileID,'%s\n%s\n',strcat('@attribute',' FundFreqA_',GSets{sensortoread},' numeric'),strcat('@attribute',' FundFreqB_',GSets{sensortoread},' numeric'));
        end
 
    end

    if(ftfilter==1)
       fprintf(fileID,'%s\n%s\n%s\n',strcat('@attribute',' ZeroCrossX_',GSets{sensortoread},' numeric'),strcat('@attribute',' ZeroCrossY_',GSets{sensortoread},' numeric'),strcat('@attribute',' ZeroCrossZ_',GSets{sensortoread},' numeric'));
        if(sensortoread==7)
            fprintf(fileID,'%s\n%s\n',strcat('@attribute',' ZeroCrossA_',GSets{sensortoread},' numeric'),strcat('@attribute',' ZeroCrossB_',GSets{sensortoread},' numeric'));
        end
 
       fprintf(fileID,'%s\n%s\n%s\n',strcat('@attribute',' FZeroCrossX_',GSets{sensortoread},' numeric'),strcat('@attribute',' FZeroCrossY_',GSets{sensortoread},' numeric'),strcat('@attribute',' FZeroCrossZ_',GSets{sensortoread},' numeric'));
        if(sensortoread==7)
            fprintf(fileID,'%s\n%s\n',strcat('@attribute',' FZeroCrossA_',GSets{sensortoread},' numeric'),strcat('@attribute',' FZeroCrossB_',GSets{sensortoread},' numeric'));
        end
 
    end
end

    %% Print Class Attributes

for k = 1:nFiles

    Nfile=strsplit(Filelist(k).name,'_');
    ClassFiles{k}=char(Nfile(2));

end

fprintf(fileID,'%s','@attribute activity {');
for k = 1:nFiles-1
    fprintf(fileID,'%s',strcat(ClassFiles{k},','));    
end
    fprintf(fileID,'%s\n\n',strcat(ClassFiles{nFiles},'}'));    

fprintf(fileID,'@data\n');
fclose(fileID);

fprintf('Processed: \n');

    %% Get feats from all files
for k = 1:nFiles
      filename = Filelist(k).name;
      
    %% Creating main Array (scanfile)
    FileToRead=fullfile(datasetspath,filename);

    fileID = fopen(FileToRead,'r');
    fSpecX3=' %f %f %f';
    fSpecX5=' %f %f %f %f %f';
    %num(1),timestamp(1),Acc(3),gyro(3),gravity(3),magnet(3),linAcc(3),tilt(3),Rotation(5)
    formatSpec = strcat('%d %f',fSpecX3,fSpecX3,fSpecX3,fSpecX3,fSpecX3,fSpecX3,fSpecX5);
    sizeM = [25 Inf];

    % Matrix_R, contains the dataset.
    Matrix_R = fscanf(fileID,formatSpec,sizeM);
    fclose(fileID);

    % Change transpose
    Matrix_R = Matrix_R';  

    % Correct limits
    if(LowerLimit<=0)
        LowerLimit=1;
    end
    UserUpperlimit=LowerLimit+WindowSize-1;
    UpperLimit=UserUpperlimit;
    if(size(Matrix_R)<UpperLimit)
        LimitSize=size(Matrix_R);
        UpperLimit=LimitSize(1);
    end

    SampleSize=UpperLimit-LowerLimit;

    
    %% Sensors Loop of Sample
    for SensorLoop=(1:SizeActSen(2))
        Sensorft=ActSen(nsensors);
        %Creating the Sample
        if(Sensorft<7)
           Sample = zeros(SampleSize,4);
            for i=(1:UpperLimit-LowerLimit)%timestamploop
                Sample(i,1) = Matrix_R(LowerLimit+i,1);
            end;

            for i=(1:UpperLimit-LowerLimit)
                for j=(2:4)
                Sample(i,j) = Matrix_R(LowerLimit+i,j+1+(Sensorft-1)*3);
                end
            end;
        else            
            Sample = zeros(SampleSize,6);
            for i=(1:UpperLimit-LowerLimit)%timestamploop
                Sample(i,1) = Matrix_R(LowerLimit+i,1);
            end;

            for i=(1:UpperLimit-LowerLimit)
                for j=(2:6)
                Sample(i,j) = Matrix_R(LowerLimit+i,j+1+(Sensorft-1)*3);
                end
            end;
        end
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


        fileID = fopen(resultgo,'a');


        % precisionprint=strcat('%',pNum,'.',pDec,'f\t','%',pNum,'.',pDec,'f\t','%',pNum,'.',pDec,'f\t');
        precisionprint=strcat('%',pNum,'.',pDec,'f,','%',pNum,'.',pDec,'f,','%',pNum,'.',pDec,'f,');


        %Printing First name of file and therefore the identifier (output Y)
        Yfile=strsplit(filename,'_');






        % Frequency Domain Features
        %----------------------------------

        %% FFT
        if(ftfft==1)

            Sampletime=Sample(SampleSize-1,1)-Sample(1,1);%in miliseconds

            NFFT = 2^nextpow2(SampleSize);

            fftX=fft(Sample(:,2));
            fftX(1)=[];
            fftY=fft(Sample(:,3));
            fftY(1)=[];
            fftZ=fft(Sample(:,4));
            fftZ(1)=[];

            nfft=length(fftX);
            powerFFTX = abs(fftX(1:floor(nfft/2))).^2;
            powerFFTY = abs(fftY(1:floor(nfft/2))).^2;
            powerFFTZ = abs(fftZ(1:floor(nfft/2))).^2;
            SampleFreq=floor(SampleSize/(Sampletime/1000));
            nyquist = SampleFreq/2;
            plotfreq=(1:nfft/2)/(nfft/2)*nyquist;

            FundfreqX = plotfreq(find(powerFFTX==max(powerFFTX)));
            FundfreqY = plotfreq(find(powerFFTY==max(powerFFTY)));
            FundfreqZ = plotfreq(find(powerFFTZ==max(powerFFTZ)));

        end   

        %% Filter & ZeroCrosseFreq/2);
        if(ftfft==1)
            fNorm = cutfreq / (SampleFreq/2);
            [b,a] = butter(2, fNorm, 'low');
            SampleMean=Sample;
            SampleMean(:,2)=Sample(:,2)-meanX;
            SampleMean(:,3)=Sample(:,3)-meanY;
            SampleMean(:,4)=Sample(:,4)-meanZ;
            FilterSample = filtfilt(b, a, SampleMean);

            %% Zero crossing 
            Hzerocross = dsp.ZeroCrossingDetector;
            ZeroCrossX = step(Hzerocross,SampleMean(:,2));
            ZeroCrossY = step(Hzerocross,SampleMean(:,3));
            ZeroCrossZ = step(Hzerocross,SampleMean(:,4));

            FZeroCrossX = step(Hzerocross,SampleMean(:,2));
            FZeroCrossY = step(Hzerocross,SampleMean(:,3));
            FZeroCrossZ = step(Hzerocross,SampleMean(:,4));
        end


        % %%TO CHECK
        %Spectral Entropy
        %    P=sum(abs(fft(data-window)).^2)
        %    %Normalization
        %    d=P(:);
        %    d=d/sum(d+ 1e-12);
        % 
        %    %Entropy Calculation
        %    logd = log2(d + 1e-12);
        %    Entropy(inc) = -sum(d.*logd)/log2(length(d));


        %% Printing Feature values
        if(ftTDomain==1)
            if(Sensorft<7)
                fprintf(fileID,precisionprint,meanX,meanY,meanZ);
                fprintf(fileID,precisionprint,stdX,stdY,stdZ);
                fprintf(fileID,precisionprint,varianceX,varianceY,varianceZ);
                fprintf(fileID,precisionprint,mMagnitude,stdMagnitude,varMagnitude);
%             else            
%                 fprintf(fileID,precisionprint,meanX,meanY,meanZ,meanA,meanB);
%                 fprintf(fileID,precisionprint,stdX,stdY,stdZ,stdA,stdB);
%                 fprintf(fileID,precisionprint,varianceX,varianceY,varianceZ,varianceA,varianceB);
%                 fprintf(fileID,precisionprint,mMagnitude,stdMagnitude,varMagnitude);
            end


        end

%         if(ftTCovariance==1)
%             if(SLoop<7)
%             fprintf(fileID,strcat(precisionprint,'%',pNum,'.',pDec,'f,'),covXY(1,1),covXY(2,1),covXY(1,2),covXY(2,2));
%             fprintf(fileID,strcat(precisionprint,'%',pNum,'.',pDec,'f,'),covXY(1,1),covXY(2,1),covXY(1,2),covXY(2,2));
%             fprintf(fileID,strcat(precisionprint,'%',pNum,'.',pDec,'f,'),covXY(1,1),covXY(2,1),covXY(1,2),covXY(2,2));
%             else            
%  
%             end
% 
%         end

        if(ftfft==1)
            if(Sensorft<7)
           fprintf(fileID,precisionprint,FundfreqX,FundfreqY,FundfreqZ);    
            else            
 
            end


        end

        if(ftfilter==1)
            
            if(Sensorft<7)
               fprintf(fileID,precisionprint,ZeroCrossX,ZeroCrossY,ZeroCrossZ); 
               fprintf(fileID,precisionprint,FZeroCrossX,FZeroCrossY,FZeroCrossZ);  
            else            
 
            end


        end
    end
        fprintf(fileID,'%s\n',char(Yfile(2)));
        fclose(fileID);
        fprintf('%s\t With\t %d lines \n', filename,UpperLimit);
    
end

