 function [FilterArray] = ButtFilter(Array,cutfreq,SampleFreq) %Returns the array filtered 
            
            FilterArray=Array;
            fNorm = cutfreq / (SampleFreq/2);
            [b,a] = butter(2, fNorm, 'low');
            FilterArray=filtfilt(b, a, Array);
                
         end