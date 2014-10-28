% Author: Luis A. Gonzalez
% Date	: 19-10-2014
% Brief	: Reads all the .txt files of a folder and gives back the .arff
% Sensors to be consider can be chosen
clearvars 

%% getting working dir
fullworkdir = mfilename('fullpath') ;
wd=strsplit(fullworkdir,'\');
wdsize=size(wd);
workdir='';
for i=(1:(wdsize(2)-1))
    workdir=fullfile(workdir,wd(i));
end

cd(char(workdir));

%% Settings
    %% File Handling 
    % Folder of Sets must not containother than Sets,
    %the data should be  '%d %d %f %f %f ...'
    %for each event shall be 2 files:
    %1 file of Sensors with name log_sensors_CUSTOMNAME_TIMESTAMP.txt
    %1 be 1 file of Networks with name log_networks_CUSTOMNAME_TIMESTAMP.txt

    sensordatasetspath  = 'C:\Users\LG\Dropbox\MDP_LAG\WatchTest1\Motion';
    networkdatasetspath = 'C:\Users\LG\Dropbox\MDP_LAG\WatchTest1\Location';
    resultsname='WR_Win100';
    resultspath='C:\Users\LG\Dropbox\MDP_LAG\WatchTest1\';
    
    
    %% Precision of Other settings
    
    pNum='4';
    pDec='4';
    fftN=500; % FFT resolution biggger the more resolution

    % Sample window
    LowerLimit=1; %Min 1
    WindowSize=100; %"inf" for using the whole set
    
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
%     ActSen=[1,2,3,4,5,6]; 
     ActSen=[1,2,3,4,5,6]; 
    
    %% arff Notes; write any coments here
FileComments='This is a 1 line comment';

%% PROGRAM STARTS

%% Sensor Part
Filelist    = dir(fullfile(sensordatasetspath, '*.txt'));
nFiles   = length(Filelist);

    %% Get Motion feats from all files
    SizeActSen=size(ActSen);
    SensorftTable=array2table(zeros(1,1+21*SizeActSen(2)));
for k = 1:nFiles
      filename = Filelist(k).name;
      
    %% Creating main Array (scanfile)
    ssplit=strsplit(filename,'_');
    
        
        FileToRead=fullfile(sensordatasetspath,filename);

        MainTable = readtable(FileToRead,'Delimiter','\t','ReadVariableNames',false);
        MainTable(:,22)=[]; %erase the empty last column
%         MainTable = readtable(FileToRead);
        Matrix_R = table2array(MainTable);
        sizeofM_R=size(Matrix_R);

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
        SensorftCell=cell(1,1+21*SizeActSen(2));

       
   %% Sensors Loop of Sample
        for nsensors=(1:SizeActSen(2))
            Sensorft=ActSen(nsensors);
            %Creating the Sample of Sensors


           SampleS = zeros(SampleSize,4);
            for i=(1:UpperLimit-LowerLimit)%timestamploop
                SampleS(i,1) = Matrix_R(LowerLimit+i,1);
            end;

            for i=(1:UpperLimit-LowerLimit)
                for j=(2:4)
                SampleS(i,j) = Matrix_R(LowerLimit+i,j+1+(Sensorft-1)*3);
                end
            end;


            % Time Domain Features
            %----------------------------------
            % time slotMean, Standard Deviation, Variance, Covariance and their Magnitudes (norm)

            %time slot
            timeMean=mean(SampleS(:,1));
            if timeMean < 060000000
                timeSlot = 1;
            elseif timeMean < 120000000
                timeSlot = 2;
            elseif timeMean < 210000000
                timeSlot = 3;
            else
                timeSlot = 4;
            end

            meanX=mean(SampleS(:,2));
            meanY=mean(SampleS(:,3));
            meanZ=mean(SampleS(:,4));

            stdX=std(SampleS(:,2));
            stdY=std(SampleS(:,3));
            stdZ=std(SampleS(:,4));


            varianceX=var(SampleS(:,2));
            varianceY=var(SampleS(:,3));
            varianceZ=var(SampleS(:,4));

%             covXY=cov(SampleS(:,2),SampleS(:,3));
%             covYZ=cov(SampleS(:,3),SampleS(:,4));
%             covZX=cov(SampleS(:,4),SampleS(:,2));

            %regression line of Covariance relation: y=mx +b
            % http://facultyweb.berry.edu/vbissonnette/statshw/doc/reg_sup.html
%                 Reg_XY_b=covXY/varianceX;   
%                 Reg_XY_m=meanY-Reg_XY_b*meanX;
% 
%                 Reg_YZ_b=covYZ/varianceY;
%                 Reg_YZ_m=meanZ-Reg_YZ_b*meanY;
% 
%                 Reg_ZX_b=covZX/varianceZ;
%                 Reg_ZX_m=meanX-Reg_ZX_b*meanZ;

            mMagnitude=sqrt(power(meanX,2)+power(meanY,2)+power(meanZ,2));
            stdMagnitude=sqrt(power(stdX,2)+power(stdY,2)+power(stdZ,2));
            varMagnitude=sqrt(power(varianceX,2)+power(varianceY,2)+power(varianceZ,2));

            %% FFT
            Sampletime = datenumtosecs(Matrix_R(UpperLimit,2))-datenumtosecs(Matrix_R(LowerLimit,2));
            NFFT = 2^nextpow2(SampleSize);
            SampleFreq=floor(SampleSize/(Sampletime));
            nyquist = SampleFreq/2;
            if(ftfft==1)
                
                if (stdX<0.001)% signals with no variation dont have FundFreq
                    FundfreqX=0;
                else              
                    fftX=fft(SampleS(:,2));
                    fftX(1)=[];
                    nfft=length(fftX);
                    powerFFTX = abs(fftX(1:floor(nfft/2))).^2;
                    plotfreq=(1:nfft/2)/(nfft/2)*nyquist;
                    FundfreqX = plotfreq(find(powerFFTX==max(powerFFTX)));
                end
                
                if (stdY<0.001)% signals with no variation dont have FundFreq
                    FundfreqY=0;
                else                       
                    fftY=fft(SampleS(:,3));
                    fftY(1)=[];
                    nfft=length(fftY);
                    powerFFTY = abs(fftY(1:floor(nfft/2))).^2;
                    plotfreq=(1:nfft/2)/(nfft/2)*nyquist;
                    FundfreqY = plotfreq(find(powerFFTY==max(powerFFTY)));
                end
                
                if (stdZ<0.001)% signals with no variation dont have FundFreq
                    FundfreqZ=0;
                else   
                    fftZ=fft(SampleS(:,4));
                    fftZ(1)=[];
                    nfft=length(fftZ);
                    powerFFTZ = abs(fftZ(1:floor(nfft/2))).^2;
                    plotfreq=(1:nfft/2)/(nfft/2)*nyquist;       
                    FundfreqZ = plotfreq(find(powerFFTZ==max(powerFFTZ)));
                end
                
            end   

            %% Filter & ZeroCrosseFreq/2);
            if(ftfft==1)
                fNorm = cutfreq / (SampleFreq/2);
                [b,a] = butter(2, fNorm, 'low');
                SampleMean=SampleS;
                SampleMean(:,2)=SampleS(:,2)-meanX;
                SampleMean(:,3)=SampleS(:,3)-meanY;
                SampleMean(:,4)=SampleS(:,4)-meanZ;
                FilterSample = filtfilt(b, a, SampleMean);

                %% Zero crossing 
                Hzerocross = dsp.ZeroCrossingDetector;
                ZeroCrossX = step(Hzerocross,SampleMean(:,2));
                ZeroCrossY = step(Hzerocross,SampleMean(:,3));
                ZeroCrossZ = step(Hzerocross,SampleMean(:,4));

                FZeroCrossX = step(Hzerocross,SampleMean(:,2));
                FZeroCrossY = step(Hzerocross,SampleMean(:,3));
                FZeroCrossZ = step(Hzerocross,SampleMean(:,4));

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


            end
            %% Sensor feats to CellArray
            
      
            sstep=(Sensorft-1)*21+1;
                       
            
            SensorftCell{1}= timeSlot;
            
            SensorftCell{1+sstep}= meanX;
            SensorftCell{2+sstep}= meanY;
            SensorftCell{3+sstep}= meanZ;
            SensorftCell{4+sstep}=stdX;
            SensorftCell{5+sstep}=stdY;
            SensorftCell{6+sstep}=stdZ;
            SensorftCell{7+sstep}=varianceX;
            SensorftCell{8+sstep}=varianceY;
            SensorftCell{9+sstep}=varianceZ;
%             SensorftCell{10+sstep}=covXY;
%             SensorftCell{11+sstep}=covYZ;
%             SensorftCell{12+sstep}=covZX;
            SensorftCell{10+sstep}=mMagnitude;
            SensorftCell{11+sstep}=stdMagnitude;
            SensorftCell{12+sstep}=varMagnitude;
%             SensorftCell{13+sstep}=0;
%             SensorftCell{14+sstep}=0;
%             SensorftCell{15+sstep}=0;
                        SensorftCell{13+sstep}=FundfreqX;
            SensorftCell{14+sstep}=FundfreqY;
            SensorftCell{15+sstep}=FundfreqZ;
            SensorftCell{16+sstep}=double(ZeroCrossX);
            SensorftCell{17+sstep}=double(ZeroCrossY);
            SensorftCell{18+sstep}=double(ZeroCrossZ);
            SensorftCell{19+sstep}=double(FZeroCrossX);
            SensorftCell{20+sstep}=double(FZeroCrossY);
            SensorftCell{21+sstep}=double(FZeroCrossZ);


        end %End of sensor feat extraction of 1 file
    
 
                    %Creating a 1 line table with all features
            Sensorft1lineTable=cell2table(SensorftCell); 
            SensorftTable.Properties.VariableNames=Sensorft1lineTable.Properties.VariableNames;
            %Concatenates the main ftTable with the line
            SensorftTable=[SensorftTable;Sensorft1lineTable]; 
    
        

        
    
    %% Merge NetworkftTable
  
    
    
end

SensorftTable([1],:)=[];
    
    %% Get Location feats from all files

run('BatchNetworkMerger.m');




%% Print
formatOut =30; %ISO 8601 timestamp

    %% Writting header in Results

resultgo = fullfile(resultspath,strcat(resultsname,'_',datestr(now,formatOut),'.arff'));
fileID = fopen(resultgo,'w');
fprintf(fileID,'%s\t%s\n\n','%',FileComments);
fprintf(fileID,'%s\n\n','@relation deviceclass');

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
    %time slot, dawn,morning,evening, night=1,2,3,4
    fprintf(fileID,'%s\n',strcat('@attribute',' timeslot',' numeric'));
        %% Motion

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

        %% Location
    for nnet=(1:sh(2))
    NameNetwork=networktable.Properties.VariableNames(nnet);

%         fprintf(fileID,'%s\n',strcat('@attribute ',' ',char(NameNetwork),' numeric'));
        fprintf(fileID,'%s %s %s\n','@attribute ',char(NameNetwork),' numeric');
    end
        %% Print Class Attributes


for k = 1:nFiles

    Nfile=strsplit(Filelist(k).name,'_');
    ClassFiles{k}=char(Nfile(3));

end

ClassFiles=unique(ClassFiles);


fprintf(fileID,'%s','@attribute activity {');
for k = 1:length(ClassFiles)-1
    fprintf(fileID,'%s',strcat(ClassFiles{k},','));    
end
    fprintf(fileID,'%s\n\n',strcat(ClassFiles{length(ClassFiles)},'}'));    

fprintf(fileID,'@data\n');
fclose(fileID);

fprintf('Processed: \n');

    %% precision
       


        % precisionprint=strcat('%',pNum,'.',pDec,'f\t','%',pNum,'.',pDec,'f\t','%',pNum,'.',pDec,'f\t');
        precisionprint=strcat('%',pNum,'.',pDec,'f,','%',pNum,'.',pDec,'f,','%',pNum,'.',pDec,'f,');


        %Printing First name of file and therefore the identifier (output Y)
        Yfile=strsplit(filename,'_');

    %% Print feat sensors
    %% Printing Feature values
    st=size(SensorftTable);
    nt=size(networktable);
    tt=st(2)+nt(2);
    
    SensorArray=table2array(SensorftTable);
    NetworkArray=table2array(networktable);
    SensorCell=table2cell(SensorftTable);
    NetworkCell=table2cell(networktable);

%         SensorArray=cell2mat(SensorCell);
%     NetworkArray=cell2mat(NetworkCell);
    
    for m=(1:nFiles)
        fileID = fopen(resultgo,'a');
        filename = Filelist(m).name;
        ssplit=strsplit(filename,'_');
        filename=ssplit(3);
        
        %print sensors
        for n=(1:st(2))
%         fprintf(fileID,strcat('%',pNum,'.',pDec,'f,'),SensorCell(m,n));
        fprintf(fileID,'%4.3f ',SensorArray(m,n));
        end
        
        %print networks
        for n=(1:nt(2))
        fprintf(fileID,'%4.3f ',NetworkArray(m,n));
        end
        
        fprintf(fileID,'%s\n',char(filename));
        
        fclose(fileID);
      fprintf('%s\t With\t %d lines \n', Filelist(m).name,UpperLimit);
    end
    
       
