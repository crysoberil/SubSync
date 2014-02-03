package subtitle.renamer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;


class Renamer
{
    private String location;
    final private HashSet<String> videoFormats;
    final private HashSet<String> subtitlesFormats;
    private HashSet<Integer> renamedSubtiles;
    private HashSet<Integer> renamedVideos;
    
    public Renamer()
    {
        // init
        videoFormats = new HashSet<String>( Arrays.asList("flv", "wmv", "mp4", "mpg", "vob", "mkv", "mov", "avi", "divx", "mpeg", "m4p", "3gp") );
        subtitlesFormats = new HashSet<String>( Arrays.asList("srt", "sub", "rt", "usf") );
        
        renamedSubtiles = new HashSet<Integer>();
        renamedVideos = new HashSet<Integer>();
    }
    
    public void setPath( String location ) throws Exception
    {
        if( location.length() == 0 )
            throw new Exception();
        
        this.location = location;
        
        if( !this.location.endsWith("\\") )
            this.location += "\\";
        
        renamedVideos.clear();
        renamedSubtiles.clear();
    }
    
    public void rename() throws Exception
    {
        File directory = new File(location);
        String [] files = directory.list();
        File file;
        Integer episodeNumber;
        String extension;
        
        for( String fileName : files )
        {
            file = new File(location+fileName);
            if( file.isFile() )
            {
                fileName = fileName.toLowerCase();
                episodeNumber = getEpisodeNumber(fileName);
                extension = getFileExtension(fileName);
                
                if( isVideoFile(extension) )
                {
                    if( !videoRenamed(episodeNumber) )
                    {
                        renameFile(file, new File(location+String.format("Episode-%d.%s", episodeNumber,extension)));
                        
//                        System.out.println(file.getName() + "->" + String.format("Episode-%d.%s\n", episodeNumber,extension));
                        renamedVideos.add(episodeNumber);
                    }
                }
                else if( isSubtitleFile(extension) )
                {
                    if( !subtitleRenamed(episodeNumber) )
                    {
                        renameFile(file, new File(location+String.format("Episode-%d.%s", episodeNumber,extension)));
//                        System.out.println(file.getName() + "->" + String.format("Episode-%d.%s\n", episodeNumber,extension));
                        renamedSubtiles.add(episodeNumber);
                    }
                }
            }
        }
    }
    
    private boolean isDigit( char c )
    {
        return c >= '0' && c <= '9';
    }

    private Integer getEpisodeNumber(String fileName)
    {
        char[] name = fileName.toCharArray();
        
        int i, epNum = 0;
        
        for( i = 0 ; i < name.length ; i++ )
        {
            if( name[i] == 'x' || name[i] == 'e' )
            {
                for( i++ ; i < name.length && name[i] == ' ' ; i++ );
                
                if( !isDigit(name[i]) )
                    continue;
                
                for( epNum = 0 ; i < name.length && isDigit(name[i]) ; i++ )
                    epNum = 10 * epNum + name[i] - 48;
                
                return epNum;
            }
            else if( isDigit(name[i]) && !immediatelyFollowedByx(name,i) )
            {
                if( i + 2 < name.length && isDigit(name[i+1]) && isDigit(name[i+2]) )
                    return ( name[i+1] - 48 ) * 10 + name[i+1] - 48;
            }
        }
        
        return 0;
    }

    private String getFileExtension(String fileName)
    {
        return fileName.substring( fileName.lastIndexOf(".") + 1 );
    }
    
    private boolean isVideoFile( String extension )
    {
        return videoFormats.contains(extension);
    }

    private boolean isSubtitleFile(String extension)
    {
        return subtitlesFormats.contains(extension);
    }

    private boolean videoRenamed(Integer episodeNumber)
    {
        return renamedVideos.contains(episodeNumber);
    }

    private boolean subtitleRenamed(Integer episodeNumber)
    {
        return renamedSubtiles.contains(episodeNumber);
    }

    private void renameFile(File file, File file2)
    {
//        System.out.print(file.exists());
        if( !file2.exists() )
        {
            try {
                Files.move(file.toPath(), file2.toPath() , StandardCopyOption.REPLACE_EXISTING );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean immediatelyFollowedByx(char[] name, int i)
    {
        for( i-- ; i >= 0 && name[i] == ' ' ; i-- );
        
        if( i == -1 )
            return false;
        return name[i] == 'x';
    }
}
