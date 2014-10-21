function [ nSeconds ] = datenumtosecs( HHMMSSmmm )
%Converts full num hour (including milliseconds)of a day to Seconds

    div=10000000;
%hours    
        nSeconds=floor(HHMMSSmmm/div)*3600;
        MMSSmmm=mod(HHMMSSmmm,div);
        div=div/100;
%minutes
        nSeconds=nSeconds+floor(MMSSmmm/div)*60;
        SSmmm=mod(MMSSmmm,div);
        div=div/100;
%seconds
        nSeconds=nSeconds+floor(SSmmm/div);
        mmm=mod(SSmmm,div);
%milliseconds
        nSeconds=nSeconds+(mmm*.001);
end

