 function [ZeroArray] = ZeroGraph(Array) %Returns the array normalized to zero 
            [row,col] = size(Array);
            ZeroArray=Array;
                for i=(1:col)
                    col_avg=mean(Array(:,i));

                    for j=(1:row)
                        ZeroArray(j,i)=Array(j,i)-col_avg;
                    end
                end
         end