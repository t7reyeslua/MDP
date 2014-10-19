classdef DataManage
    %UNTITLED3 Summary of this class goes here
    %   Detailed explanation goes here
    
%     properties
%     end
    
    methods
        function Z_G=ZeroG(Array) %Returns the array normalized to zero 
            [row,col] = size(Array);
            ZeroArray=Array;

                for i=(1:col)
                    col_avg=mean(Array(:,i));

                    for j=(1:raw)
                        ZeroArray(j,i)=Array(j,i)-col_avg;
                    end
                end
         end
    end
    
end

