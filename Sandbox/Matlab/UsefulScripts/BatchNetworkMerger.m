% clearvars
networkdatasetspath = 'C:\Users\LG\Dropbox\MDP_LAG\TestScan\Location';
NetFilelist    = dir(fullfile(networkdatasetspath, '*.txt'));
nFiles   = length(NetFilelist);

%% Get Main header
    filename = NetFilelist(1).name;
    FileToRead=fullfile(networkdatasetspath,filename);
    ttable=readtable(FileToRead);
    headers=ttable.Properties.VariableNames;


for k = 2:nFiles
    filename = NetFilelist(k).name;
    FileToRead=fullfile(networkdatasetspath,filename);
    ttable=readtable(FileToRead);
    
    newheaders=ttable.Properties.VariableNames;
    sn=size(newheaders);
    
    for i=(1:sn(2))
        SameFlag=0;
        sh=size(headers);
        for j=(1:sh(2))           
            if (strcmp(headers(j),newheaders(i)))
                SameFlag=1;
            end        
        end 
        if (SameFlag==0)
            headers{j+1}=char(newheaders(i));
        end 
    end
end

headers=sort(headers);

%% Get Coincidence Cell Vectors
    sh=size(headers);
    IntersectionCell=cell(nFiles,sh(2)); %cell array containing the coincident networks, else is zero
    IntersectionCell(:) = {zeros(nFiles)};
    
for k = 1:nFiles
    filename = NetFilelist(k).name;
    FileToRead=fullfile(networkdatasetspath,filename);
    ttable=readtable(FileToRead);
    sn=size(ttable);
   

    for nHeader=(1:sh(2))
        CoinFlag=0;
        for nNet=(1:sn(2))           

            A=char(headers(nHeader));
            B=char(ttable.Properties.VariableNames(nNet));
            if (strcmp(A,B))
                IntersectionCell{k,nHeader}=table2array(ttable(1,nNet));
                CoinFlag=1;%to check if there was a coincidence or not
             
            end
                
        end

        if (CoinFlag==0)
            IntersectionCell{k,nHeader}=0;

        end       

    end
    
end

%% 
networktable=cell2table(IntersectionCell);
networktable.Properties.VariableNames=headers;
