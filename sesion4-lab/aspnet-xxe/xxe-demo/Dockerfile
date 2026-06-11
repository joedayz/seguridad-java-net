FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY XxeDemo.csproj .
RUN dotnet restore
COPY . .
RUN dotnet publish -c Release -o /app/publish /p:UseAppHost=false

FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
COPY --from=build /app/publish .
COPY demo-secrets ./demo-secrets
ENV ASPNETCORE_URLS=http://+:8188
EXPOSE 8188
ENTRYPOINT ["dotnet", "XxeDemo.dll"]
